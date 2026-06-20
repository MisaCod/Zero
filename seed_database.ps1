
# ============================================================================
# seed_database.ps1 — Siembra completa de la base de datos 0 Grados en Supabase
# Ejecutar: powershell -ExecutionPolicy Bypass -File seed_database.ps1
# ============================================================================

$SB_URL = "https://ehbrpeqfiebzafxmwgsn.supabase.co"
$SB_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVoYnJwZXFmaWViemFmeG13Z3NuIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3ODA3Njk0MiwiZXhwIjoyMDkzNjUyOTQyfQ.F7cLcf0QFMfUgi93aYj3YZubls62am16bS8ZXINlkms"

function Post-SB($endpoint, $body) {
    try {
        $r = Invoke-RestMethod -Uri "$SB_URL/rest/v1/$endpoint" `
            -Method POST `
            -Headers @{
                "apikey"        = $SB_KEY
                "Authorization" = "Bearer $SB_KEY"
                "Content-Type"  = "application/json"
                "Accept"        = "application/json"
                "Prefer"        = "resolution=merge-duplicates,return=representation"
            } `
            -Body $body `
            -ErrorAction Stop
        return $r
    } catch {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $err = $reader.ReadToEnd()
        Write-Host "  ERROR en $endpoint`: $err" -ForegroundColor Red
        return $null
    }
}

Write-Host "`n🗄️  Seeding 0 Grados database..." -ForegroundColor Cyan

# ── PASO 1: equipment_type ──────────────────────────────────────────────────
Write-Host "`n[1/7] equipment_type..." -ForegroundColor Yellow
$types = Post-SB "equipment_type" '[
  {"id":"11111111-0000-0000-0000-000000000001","name":"Compresor"},
  {"id":"11111111-0000-0000-0000-000000000002","name":"Chiller"},
  {"id":"11111111-0000-0000-0000-000000000003","name":"Motor Electrico"},
  {"id":"11111111-0000-0000-0000-000000000004","name":"Tablero Electrico"},
  {"id":"11111111-0000-0000-0000-000000000005","name":"Unidad Condensadora"}
]'
if ($types) { Write-Host "  ✅ $($types.Count) tipos insertados" -ForegroundColor Green }

# ── PASO 2: equipment_catalog ───────────────────────────────────────────────
Write-Host "`n[2/7] equipment_catalog..." -ForegroundColor Yellow
$catalog = Post-SB "equipment_catalog" '[
  {"id":"22222222-0000-0000-0000-000000000001","type_id":"11111111-0000-0000-0000-000000000001","brand_id":null,"model":"C-40","description":"Compresor Industrial de alta capacidad 40 Ton","refrigerant":"R-22","capacity_tons":40},
  {"id":"22222222-0000-0000-0000-000000000002","type_id":"11111111-0000-0000-0000-000000000002","brand_id":null,"model":"30XA","description":"Chiller Carrier scroll 30 Ton","refrigerant":"R-410A","capacity_tons":30},
  {"id":"22222222-0000-0000-0000-000000000003","type_id":"11111111-0000-0000-0000-000000000003","brand_id":null,"model":"WEG-15HP","description":"Motor Trifasico WEG 15HP 220V","refrigerant":null,"capacity_tons":null},
  {"id":"22222222-0000-0000-0000-000000000004","type_id":"11111111-0000-0000-0000-000000000004","brand_id":null,"model":"T-105","description":"Tablero Electrico trifasico 200A","refrigerant":null,"capacity_tons":null}
]'
if ($catalog) { Write-Host "  ✅ $($catalog.Count) catálogos insertados" -ForegroundColor Green }

# ── PASO 3: client_equipment ────────────────────────────────────────────────
Write-Host "`n[3/7] client_equipment..." -ForegroundColor Yellow
$clientId = "b4eaec60-210f-4ec9-bcc4-8ae7b93a5f83"
$clientEq = Post-SB "client_equipment" "[
  {`"id`":`"33333333-0000-0000-0000-000000000001`",`"client_id`":`"$clientId`",`"catalog_id`":`"22222222-0000-0000-0000-000000000001`",`"serial_num`":`"SN-C40-2021-001`",`"location`":`"Planta Baja - Sector B`"},
  {`"id`":`"33333333-0000-0000-0000-000000000002`",`"client_id`":`"$clientId`",`"catalog_id`":`"22222222-0000-0000-0000-000000000002`",`"serial_num`":`"SN-30XA-2020-007`",`"location`":`"Azotea - Bloque A`"},
  {`"id`":`"33333333-0000-0000-0000-000000000003`",`"client_id`":`"$clientId`",`"catalog_id`":`"22222222-0000-0000-0000-000000000003`",`"serial_num`":`"SN-WEG-2019-014`",`"location`":`"Linea de Montaje 4`"},
  {`"id`":`"33333333-0000-0000-0000-000000000004`",`"client_id`":`"$clientId`",`"catalog_id`":`"22222222-0000-0000-0000-000000000004`",`"serial_num`":`"SN-T105-2022-002`",`"location`":`"Cuarto Tecnico G`"}
]"
if ($clientEq) { Write-Host "  ✅ $($clientEq.Count) equipos del cliente insertados" -ForegroundColor Green }

# ── PASO 4: service_request ─────────────────────────────────────────────────
Write-Host "`n[4/7] service_request..." -ForegroundColor Yellow
$sr1id = "44444444-0000-0000-0000-000000000001"
$sr2id = "44444444-0000-0000-0000-000000000002"
$sr3id = "44444444-0000-0000-0000-000000000003"
$sr4id = "44444444-0000-0000-0000-000000000004"

$requests = Post-SB "service_request" "[
  {`"id`":`"$sr1id`",`"client_id`":`"$clientId`",`"equipment_id`":`"33333333-0000-0000-0000-000000000001`",`"status`":`"SIN INICIAR`",`"failure_desc`":`"Compresor C-40 no alcanza temperatura de enfriamiento. Temperatura ambiente 35C, salida 28C (deberia ser 18C). Posible falla en valvula de expansion.`"},
  {`"id`":`"$sr2id`",`"client_id`":`"$clientId`",`"equipment_id`":`"33333333-0000-0000-0000-000000000002`",`"status`":`"EN PROGRESO`",`"failure_desc`":`"Chiller Carrier 30XA presenta fuga de refrigerante R-410A. Presion cayo de 380 a 210 PSI en 48 horas. Requiere recarga y sellado urgente.`"},
  {`"id`":`"$sr3id`",`"client_id`":`"$clientId`",`"equipment_id`":`"33333333-0000-0000-0000-000000000003`",`"status`":`"TERMINADO`",`"failure_desc`":`"Motor WEG 15HP vibracion excesiva en rodamientos traseros. Se realizó cambio de rodamientos 6205-ZZ y realineacion del eje.`"},
  {`"id`":`"$sr4id`",`"client_id`":`"$clientId`",`"equipment_id`":`"33333333-0000-0000-0000-000000000004`",`"status`":`"SIN INICIAR`",`"failure_desc`":`"Tablero T-105 registra fluctuaciones de voltaje entre 198V y 225V. Contactor principal presenta arcos electricos al cerrar.`"}
]"
if ($requests) { Write-Host "  ✅ $($requests.Count) solicitudes insertadas" -ForegroundColor Green }

# ── PASO 5: assigments (asignar técnico a las solicitudes en progreso) ──────
Write-Host "`n[5/7] assigments..." -ForegroundColor Yellow
$tecnicoId = "63200d12-e7c7-481f-ab8b-cb1f618b348f"
$assignments = Post-SB "assigments" "[
  {`"id`":`"55555555-0000-0000-0000-000000000001`",`"request_id`":`"$sr2id`",`"technician_id`":`"$tecnicoId`"},
  {`"id`":`"55555555-0000-0000-0000-000000000002`",`"request_id`":`"$sr3id`",`"technician_id`":`"$tecnicoId`"}
]"
if ($assignments) { Write-Host "  ✅ $($assignments.Count) asignaciones insertadas" -ForegroundColor Green }

# ── PASO 6: technical_report (para la solicitud terminada) ──────────────────
Write-Host "`n[6/7] technical_report..." -ForegroundColor Yellow
$reports = Post-SB "technical_report" '[
  {"id":"66666666-0000-0000-0000-000000000001","assigment_id":"55555555-0000-0000-0000-000000000002","diagnosis":"Rodamientos traseros desgastados por falta de lubricacion. Temperatura de operacion 85C (max 65C).","work_done":true,"start_time":"2026-06-18T08:00:00","end_time":"2026-06-18T11:30:00"}
]'
if ($reports) { Write-Host "  ✅ $($reports.Count) reportes insertados" -ForegroundColor Green }

# ── PASO 7: service_rating ──────────────────────────────────────────────────
Write-Host "`n[7/7] service_rating..." -ForegroundColor Yellow
$schemaRating = Invoke-RestMethod -Uri "$SB_URL/rest/v1/" -Headers @{"apikey"=$SB_KEY;"Authorization"="Bearer $SB_KEY";"Accept"="application/json"}
$ratingCols = $schemaRating.definitions.service_rating.properties.PSObject.Properties.Name
Write-Host "  Rating columns: $($ratingCols -join ', ')"

if ($ratingCols -contains "request_id") {
    $rating = Post-SB "service_rating" '[{"request_id":"44444444-0000-0000-0000-000000000003","rating":5,"comment":"Excelente servicio, resolvieron el problema rapido y bien explicado."}]'
    if ($rating) { Write-Host "  ✅ Rating insertada" -ForegroundColor Green }
}

Write-Host "`n✅ Seed completo!" -ForegroundColor Green
Write-Host "   Usuarios de prueba:" -ForegroundColor White
Write-Host "   📧 cliente@test.com     / password: 1234  (rol: CLIENTE)" -ForegroundColor White
Write-Host "   📧 tecnico@test.com     / password: 1234  (rol: TÉCNICO)" -ForegroundColor White
Write-Host "   Email: supervisor@0grados.com / password: 1234  (rol: SUPERVISOR)" -ForegroundColor White
