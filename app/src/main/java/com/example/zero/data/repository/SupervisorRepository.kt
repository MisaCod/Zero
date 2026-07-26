package com.example.zero.data.repository

import com.example.zero.data.SupabaseClient
import com.example.zero.data.model.Assignment
import com.example.zero.data.model.Profile
import com.example.zero.data.model.ServiceRequest
import com.example.zero.data.model.TechnicianDetails
import com.example.zero.data.model.User
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// ============================================================================
// SupervisorRepository — Operaciones de datos para el rol SUPERVISOR.
//
// Usa authClient (service_role) en todas las operaciones que necesitan
// bypassar RLS y ver datos de todos los usuarios.
// ============================================================================

// ── DTOs enriquecidos (resultado de joins manuales) ──────────────────────────

data class AvailableTechnician(
    val userId: String,
    val name: String,
    val lastName: String,
    val email: String,
    val score: Double,
    val identityCard: String,
)

data class UserWithProfile(
    val user: User,
    val profile: Profile?,
) {
    val fullName: String get() = if (profile != null) "${profile.name} ${profile.lastName}".trim() else user.email
    val identityCard: String get() = profile?.identityCard ?: "—"
}

data class MaintenanceReportRow(
    val requestId: String,
    val serialNum: String,
    val failureDesc: String,
    val status: String,
    val technicianName: String,
    val startTime: String?,
    val endTime: String?,
    val createdAt: String?,
    val location: String,
)

data class TechnicianPerformanceRow(
    val userId: String,
    val name: String,
    val email: String,
    val score: Double,
    val completedAssignments: Int,
    val averageRating: Double,
)

data class PartsInventoryRow(
    val id: String,
    val partName: String,
    val brandName: String,
    val clientName: String,
    val serialNum: String,
    val location: String,
    val createdAt: String?,
)

@Serializable
private data class ClientEquipmentRaw(
    val id: String = "",
    @SerialName("client_id") val clientId: String = "",
    @SerialName("catalog_id") val catalogId: String? = null,
    @SerialName("serial_num") val serialNum: String = "",
    val location: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
private data class TechnicalReportRaw(
    val id: String? = null,
    @SerialName("assigment_id") val assigmentId: String = "",
    val diagnosis: String = "",
    @SerialName("work_done") val workDone: Boolean = false,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
)

@Serializable
private data class AssignmentRaw(
    val id: String? = null,
    @SerialName("request_id") val requestId: String = "",
    @SerialName("technician_id") val technicianId: String = "",
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
private data class ServiceRatingRaw(
    @SerialName("assigment_id") val assigmentId: String = "",
    val score: Double = 0.0,
    val comments: String? = null,
)

@Serializable
private data class SparePart(
    val id: String = "",
    val name: String = "",
    @SerialName("brand_name") val brandName: String = "",
)

@Serializable
private data class ClientPartsInventory(
    val id: String = "",
    @SerialName("client_id") val clientId: String = "",
    @SerialName("parts_id") val partsId: String = "",
    @SerialName("equipment_id") val equipmentId: String? = null,
    @SerialName("serial_num") val serialNum: String = "",
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class EquipmentCatalogRaw(
    val id: String = "",
    @SerialName("type_id") val typeId: String? = null,
    @SerialName("brand_id") val brandId: String? = null,
    val reference: String = "",
)

class SupervisorRepository {

    private val auth = SupabaseClient.authClient

    suspend fun getAvailableTechnicians(): Result<List<AvailableTechnician>> {
        return try {
            val details = auth.postgrest
                .from("technician_details")
                .select { filter { eq("availability", true) } }
                .decodeList<TechnicianDetails>()
            val users = auth.postgrest.from("users").select().decodeList<User>()
            val profiles = auth.postgrest.from("profile").select().decodeList<Profile>()
            val userMap = users.associateBy { it.id ?: "" }
            val profileMap = profiles.associateBy { it.userId }
            val result = details.mapNotNull { td ->
                val user = userMap[td.userId] ?: return@mapNotNull null
                val profile = profileMap[td.userId]
                AvailableTechnician(
                    userId = td.userId,
                    name = profile?.name ?: "",
                    lastName = profile?.lastName ?: "",
                    email = user.email,
                    score = td.score ?: 0.0,
                    identityCard = profile?.identityCard ?: "—",
                )
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServiceRequest(requestId: String): Result<ServiceRequest> {
        return try {
            val requests = auth.postgrest
                .from("service_request")
                .select { filter { eq("id", requestId) } }
                .decodeList<ServiceRequest>()
            if (requests.isEmpty()) Result.failure(Exception("Solicitud no encontrada"))
            else Result.success(requests.first())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retorna Pair(location, serialNum) del equipo — tipos simples para no exponer ClientEquipmentRaw.
     */
    suspend fun getEquipmentForRequest(equipmentId: String): Result<Pair<String, String>> {
        return try {
            val equips = auth.postgrest
                .from("client_equipment")
                .select { filter { eq("id", equipmentId) } }
                .decodeList<ClientEquipmentRaw>()
            if (equips.isEmpty()) Result.failure(Exception("Equipo no encontrado"))
            else {
                val eq = equips.first()
                Result.success(Pair(eq.location ?: "", eq.serialNum))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignTechnician(requestId: String, technicianId: String): Result<Unit> {
        return try {
            val assignment = AssignmentRaw(requestId = requestId, technicianId = technicianId)
            auth.postgrest.from("assigments").insert(assignment)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setTechnicianAvailability(technicianId: String, available: Boolean): Result<Unit> {
        return try {
            auth.postgrest
                .from("technician_details")
                .update({ set("availability", available) }) {
                    filter { eq("user_id", technicianId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsersWithProfiles(): Result<List<UserWithProfile>> {
        return try {
            val users = auth.postgrest
                .from("users")
                .select { order("created_at", Order.DESCENDING) }
                .decodeList<User>()
            val profiles = auth.postgrest.from("profile").select().decodeList<Profile>()
            val profileMap = profiles.associateBy { it.userId }
            val result = users.map { u ->
                UserWithProfile(user = u, profile = profileMap[u.id ?: ""])
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUser(email: String, passwordHash: String, role: String): Result<User> {
        return try {
            val newUser = User(email = email, passwordHash = passwordHash, role = role)
            val created = auth.postgrest
                .from("users")
                .insert(newUser) { select() }
                .decodeSingle<User>()
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProfile(userId: String, name: String, lastName: String, identityCard: String, phoneNumber: String): Result<Unit> {
        return try {
            // phone_number es VARCHAR(11) en BD — truncar si el usuario puso prefijo +58
            val cleanPhone = phoneNumber.filter { it.isDigit() }.takeLast(11)
            val profile = Profile(
                userId = userId,
                name = name,
                lastName = lastName,
                identityCard = identityCard.take(20),
                phoneNumber = cleanPhone.ifBlank { null },
            )
            auth.postgrest.from("profile").insert(profile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTechnicianDetails(userId: String): Result<Unit> {
        return try {
            val td = TechnicianDetails(userId = userId, availability = true, score = 0.0)
            auth.postgrest.from("technician_details").insert(td)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(userId: String, name: String, lastName: String, phoneNumber: String, role: String): Result<Unit> {
        return try {
            auth.postgrest.from("users").update({ set("role", role) }) { filter { eq("id", userId) } }
            auth.postgrest.from("profile").update({
                set("name", name)
                set("last_name", lastName)
                set("phone_number", phoneNumber)
            }) { filter { eq("user_id", userId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            auth.postgrest.from("users").delete { filter { eq("id", userId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMaintenanceReport(): Result<List<MaintenanceReportRow>> {
        return try {
            val requests = auth.postgrest.from("service_request").select { order("created_at", Order.DESCENDING) }.decodeList<ServiceRequest>()
            val equipments = auth.postgrest.from("client_equipment").select().decodeList<ClientEquipmentRaw>()
            val assignments = auth.postgrest.from("assigments").select().decodeList<AssignmentRaw>()
            val reports = auth.postgrest.from("technical_report").select().decodeList<TechnicalReportRaw>()
            val profiles = auth.postgrest.from("profile").select().decodeList<Profile>()
            val equipMap = equipments.associateBy { it.id }
            val assignMap = assignments.associateBy { it.requestId }
            val reportMap = reports.associateBy { it.assigmentId }
            val profileMap = profiles.associateBy { it.userId }
            val rows = requests.map { req ->
                val equip = req.equipmentId?.let { equipMap[it] }
                val assign = req.id?.let { assignMap[it] }
                val report = assign?.id?.let { reportMap[it] }
                val techProfile = assign?.technicianId?.let { profileMap[it] }
                val techName = techProfile?.fullName?.takeIf { it.isNotBlank() } ?: assign?.technicianId?.take(8) ?: "Sin asignar"
                MaintenanceReportRow(
                    requestId = req.id ?: "",
                    serialNum = equip?.serialNum ?: "—",
                    failureDesc = req.failureDesc.take(80),
                    status = req.status,
                    technicianName = techName,
                    startTime = report?.startTime,
                    endTime = report?.endTime,
                    createdAt = req.createdAt,
                    location = equip?.location ?: "—",
                )
            }
            Result.success(rows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTechnicianPerformance(): Result<List<TechnicianPerformanceRow>> {
        return try {
            val details = auth.postgrest.from("technician_details").select().decodeList<TechnicianDetails>()
            val users = auth.postgrest.from("users").select().decodeList<User>()
            val profiles = auth.postgrest.from("profile").select().decodeList<Profile>()
            val assignments = auth.postgrest.from("assigments").select().decodeList<AssignmentRaw>()
            val ratings = auth.postgrest.from("service_rating").select().decodeList<ServiceRatingRaw>()
            val requests = auth.postgrest.from("service_request").select().decodeList<ServiceRequest>()
            val userMap = users.associateBy { it.id ?: "" }
            val profileMap = profiles.associateBy { it.userId }
            val requestMap = requests.associateBy { it.id ?: "" }
            val ratingMap = ratings.associateBy { it.assigmentId }
            val rows = details.mapNotNull { td ->
                val user = userMap[td.userId] ?: return@mapNotNull null
                val profile = profileMap[td.userId]
                val techAssignments = assignments.filter { it.technicianId == td.userId }
                val completed = techAssignments.count { a -> requestMap[a.requestId]?.status == "TERMINADO" }
                val ratingScores = techAssignments.mapNotNull { a -> a.id?.let { ratingMap[it]?.score } }
                val avgRating = if (ratingScores.isEmpty()) 0.0 else ratingScores.average()
                val name = profile?.fullName?.takeIf { it.isNotBlank() } ?: user.email
                val officialScore = td.score ?: avgRating
                TechnicianPerformanceRow(userId = td.userId, name = name, email = user.email, score = officialScore, completedAssignments = completed, averageRating = avgRating)
            }
            Result.success(rows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPartsInventoryReport(): Result<List<PartsInventoryRow>> {
        return try {
            val inventory = auth.postgrest.from("client_parts_inventory").select { order("created_at", Order.DESCENDING) }.decodeList<ClientPartsInventory>()
            val parts = auth.postgrest.from("spare_parts").select().decodeList<SparePart>()
            val profiles = auth.postgrest.from("profile").select().decodeList<Profile>()
            val equipments = auth.postgrest.from("client_equipment").select().decodeList<ClientEquipmentRaw>()
            val partsMap = parts.associateBy { it.id }
            val profileMap = profiles.associateBy { it.userId }
            val equipMap = equipments.associateBy { it.id }
            val rows = inventory.map { inv ->
                val part = partsMap[inv.partsId]
                val profile = profileMap[inv.clientId]
                val equip = inv.equipmentId?.let { equipMap[it] }
                PartsInventoryRow(id = inv.id, partName = part?.name ?: "—", brandName = part?.brandName ?: "—", clientName = profile?.fullName?.takeIf { it.isNotBlank() } ?: inv.clientId.take(8), serialNum = inv.serialNum, location = equip?.location ?: "—", createdAt = inv.createdAt)
            }
            Result.success(rows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServiceRequestCount(): Result<Int> {
        return try {
            val list = auth.postgrest.from("service_request").select().decodeList<ServiceRequest>()
            Result.success(list.size)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getClientEquipmentCount(): Result<Int> {
        return try {
            val list = auth.postgrest.from("client_equipment").select().decodeList<ClientEquipmentRaw>()
            Result.success(list.size)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getUserCount(): Result<Int> {
        return try {
            val list = auth.postgrest.from("users").select().decodeList<User>()
            Result.success(list.size)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun exportServiceRequestsCsv(): Result<String> {
        return try {
            val requests = auth.postgrest.from("service_request").select { order("created_at", Order.DESCENDING) }.decodeList<ServiceRequest>()
            val profiles = auth.postgrest.from("profile").select().decodeList<Profile>()
            val profileMap = profiles.associateBy { it.userId }
            val sb = StringBuilder()
            sb.appendLine("ID,Cliente,Equipo ID,Estado,Descripción,Fecha Creación")
            for (r in requests) {
                val client = profileMap[r.clientId]?.fullName ?: r.clientId.take(8)
                sb.appendLine("${r.id},\"$client\",${r.equipmentId ?: ""},${r.status},\"${r.failureDesc.replace("\"","'").take(100)}\",${r.createdAt ?: ""}")
            }
            Result.success(sb.toString())
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun exportClientEquipmentCsv(): Result<String> {
        return try {
            val equipments = auth.postgrest.from("client_equipment").select { order("created_at", Order.DESCENDING) }.decodeList<ClientEquipmentRaw>()
            val profiles = auth.postgrest.from("profile").select().decodeList<Profile>()
            val catalogs = auth.postgrest.from("equipment_catalog").select().decodeList<EquipmentCatalogRaw>()
            val profileMap = profiles.associateBy { it.userId }
            val catalogMap = catalogs.associateBy { it.id }
            val sb = StringBuilder()
            sb.appendLine("ID,Cliente,Número de Serie,Ubicación,Referencia Catálogo,Fecha Registro")
            for (e in equipments) {
                val client = profileMap[e.clientId]?.fullName ?: e.clientId.take(8)
                val catalog = e.catalogId?.let { catalogMap[it] }?.reference ?: "—"
                sb.appendLine("${e.id},\"$client\",${e.serialNum},\"${e.location ?: ""}\",\"$catalog\",${e.createdAt ?: ""}")
            }
            Result.success(sb.toString())
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun exportUsersCsv(): Result<String> {
        return try {
            val users = auth.postgrest.from("users").select { order("created_at", Order.DESCENDING) }.decodeList<User>()
            val profiles = auth.postgrest.from("profile").select().decodeList<Profile>()
            val profileMap = profiles.associateBy { it.userId }
            val sb = StringBuilder()
            sb.appendLine("ID,Email,Rol,Nombre,Apellido,Cédula,Teléfono,Fecha Registro")
            for (u in users) {
                val p = profileMap[u.id ?: ""]
                sb.appendLine("${u.id},${u.email},${u.role},\"${p?.name ?: ""}\",\"${p?.lastName ?: ""}\",${p?.identityCard ?: ""},${p?.phoneNumber ?: ""},${u.createdAt ?: ""}")
            }
            Result.success(sb.toString())
        } catch (e: Exception) { Result.failure(e) }
    }
}
