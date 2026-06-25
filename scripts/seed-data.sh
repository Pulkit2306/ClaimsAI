#!/bin/bash
# Seed dummy data into the platform
# Usage: ./scripts/seed-data.sh [BASE_URL]

BASE_URL="${1:-http://localhost:8080}"
echo "Seeding data to $BASE_URL..."

# Wait for services
echo "Waiting for API gateway..."
until curl -sf "$BASE_URL/actuator/health" > /dev/null 2>&1; do sleep 3; done
echo "Gateway is ready!"

# Register admin user
echo "Creating users..."
curl -s -X POST "$BASE_URL/api/auth/register" -H "Content-Type: application/json" \
  -d '{"firstName":"Admin","lastName":"User","email":"admin@claimsplatform.com","password":"password123","role":"ADMIN"}' > /dev/null

curl -s -X POST "$BASE_URL/api/auth/register" -H "Content-Type: application/json" \
  -d '{"firstName":"Amélie","lastName":"Desrosiers","email":"amelie@claimsplatform.com","password":"password123","role":"ADJUSTER"}' > /dev/null

curl -s -X POST "$BASE_URL/api/auth/register" -H "Content-Type: application/json" \
  -d '{"firstName":"Luc","lastName":"Moreau","email":"luc@claimsplatform.com","password":"password123","role":"UNDERWRITER"}' > /dev/null

# Get token
TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" -H "Content-Type: application/json" \
  -d '{"email":"admin@claimsplatform.com","password":"password123"}' | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
AUTH="Authorization: Bearer $TOKEN"

# Customers
echo "Creating customers..."
CUSTOMERS=(
  '{"firstName":"Marie","lastName":"Tremblay","email":"marie.tremblay@gmail.com","phone":"514-555-0101","address":"1234 Rue Saint-Denis","city":"Montreal","province":"QC","postalCode":"H2X 3K7","dateOfBirth":"1985-03-15"}'
  '{"firstName":"Jean","lastName":"Bouchard","email":"jean.bouchard@outlook.com","phone":"514-555-0102","address":"567 Boulevard René-Lévesque","city":"Montreal","province":"QC","postalCode":"H3B 1Y6","dateOfBirth":"1978-07-22"}'
  '{"firstName":"Sophie","lastName":"Gagnon","email":"sophie.gagnon@yahoo.ca","phone":"418-555-0103","address":"89 Avenue Cartier","city":"Quebec City","province":"QC","postalCode":"G1R 2S9","dateOfBirth":"1992-11-08"}'
  '{"firstName":"Pierre","lastName":"Lavoie","email":"pierre.lavoie@bell.ca","phone":"450-555-0104","address":"2345 Rue Principale","city":"Laval","province":"QC","postalCode":"H7V 1A1","dateOfBirth":"1970-01-30"}'
  '{"firstName":"Isabelle","lastName":"Côté","email":"isabelle.cote@videotron.ca","phone":"819-555-0105","address":"456 Rue Wellington","city":"Sherbrooke","province":"QC","postalCode":"J1H 5C7","dateOfBirth":"1988-06-12"}'
  '{"firstName":"François","lastName":"Roy","email":"francois.roy@gmail.com","phone":"514-555-0106","address":"7890 Avenue du Parc","city":"Montreal","province":"QC","postalCode":"H3N 1X7","dateOfBirth":"1995-09-25"}'
  '{"firstName":"Nathalie","lastName":"Pelletier","email":"nathalie.pelletier@hotmail.com","phone":"438-555-0107","address":"321 Rue Sherbrooke","city":"Montreal","province":"QC","postalCode":"H2X 1E3","dateOfBirth":"1982-04-18"}'
  '{"firstName":"Marc","lastName":"Bergeron","email":"marc.bergeron@outlook.com","phone":"514-555-0108","address":"654 Boulevard Saint-Laurent","city":"Montreal","province":"QC","postalCode":"H2T 1S1","dateOfBirth":"1975-12-03"}'
)
for c in "${CUSTOMERS[@]}"; do
  curl -s -X POST "$BASE_URL/api/customers" -H "Content-Type: application/json" -H "$AUTH" -d "$c" > /dev/null
done

# Policies
echo "Creating policies..."
POLICIES=(
  '{"customerId":1,"policyType":"AUTO","premiumAmount":1200,"coverageAmount":50000,"deductible":500,"startDate":"2025-01-01","endDate":"2026-01-01","description":"Full coverage auto - 2023 Honda Civic"}'
  '{"customerId":1,"policyType":"HOME","premiumAmount":2400,"coverageAmount":350000,"deductible":1000,"startDate":"2025-03-15","endDate":"2026-03-15","description":"Homeowner insurance - 1234 Rue Saint-Denis, Montreal"}'
  '{"customerId":2,"policyType":"AUTO","premiumAmount":1800,"coverageAmount":75000,"deductible":750,"startDate":"2025-02-01","endDate":"2026-02-01","description":"Full coverage - 2024 Toyota RAV4"}'
  '{"customerId":2,"policyType":"HEALTH","premiumAmount":3600,"coverageAmount":100000,"deductible":250,"startDate":"2025-01-01","endDate":"2026-01-01","description":"Family health plan - dental and vision included"}'
  '{"customerId":3,"policyType":"HOME","premiumAmount":1900,"coverageAmount":275000,"deductible":1500,"startDate":"2025-06-01","endDate":"2026-06-01","description":"Condo insurance - 89 Avenue Cartier, Quebec City"}'
  '{"customerId":4,"policyType":"AUTO","premiumAmount":950,"coverageAmount":35000,"deductible":500,"startDate":"2025-04-01","endDate":"2026-04-01","description":"Basic coverage - 2021 Hyundai Elantra"}'
  '{"customerId":4,"policyType":"COMMERCIAL","premiumAmount":5500,"coverageAmount":500000,"deductible":2500,"startDate":"2025-01-01","endDate":"2026-01-01","description":"Commercial property - Lavoie Auto Parts warehouse"}'
  '{"customerId":5,"policyType":"HOME","premiumAmount":1600,"coverageAmount":220000,"deductible":1000,"startDate":"2025-05-01","endDate":"2026-05-01","description":"Rental property insurance - 456 Rue Wellington"}'
  '{"customerId":6,"policyType":"AUTO","premiumAmount":2200,"coverageAmount":85000,"deductible":500,"startDate":"2025-07-01","endDate":"2026-07-01","description":"Premium coverage - 2025 Tesla Model 3"}'
  '{"customerId":7,"policyType":"TRAVEL","premiumAmount":450,"coverageAmount":50000,"deductible":100,"startDate":"2026-01-01","endDate":"2026-12-31","description":"Annual travel insurance - worldwide coverage"}'
  '{"customerId":7,"policyType":"HEALTH","premiumAmount":2800,"coverageAmount":75000,"deductible":500,"startDate":"2025-01-01","endDate":"2026-01-01","description":"Individual health plan with prescription coverage"}'
  '{"customerId":8,"policyType":"AUTO","premiumAmount":1450,"coverageAmount":60000,"deductible":750,"startDate":"2025-09-01","endDate":"2026-09-01","description":"Full coverage - 2024 Subaru Outback"}'
)
for p in "${POLICIES[@]}"; do
  curl -s -X POST "$BASE_URL/api/policies" -H "Content-Type: application/json" -H "$AUTH" -d "$p" > /dev/null
done

# Claims
echo "Creating claims..."
CLAIMS=(
  '{"policyId":1,"customerId":1,"claimType":"AUTO_COLLISION","description":"Rear-ended at intersection of Rue Saint-Denis and Boulevard de Maisonneuve. Other driver ran red light. Significant damage to rear bumper, trunk, and tail lights. Police report filed.","incidentDate":"2026-06-10","estimatedAmount":8500}'
  '{"policyId":2,"customerId":1,"claimType":"HOME_WATER_DAMAGE","description":"Burst pipe in basement during spring thaw. Water damage to finished basement including flooring, drywall, and electrical. Mold remediation may be required.","incidentDate":"2026-04-02","estimatedAmount":22000}'
  '{"policyId":3,"customerId":2,"claimType":"AUTO_THEFT","description":"Vehicle stolen from underground parking at Place Ville Marie overnight. Security cameras show suspect at 2:47 AM. Vehicle not yet recovered.","incidentDate":"2026-05-28","estimatedAmount":45000}'
  '{"policyId":4,"customerId":2,"claimType":"HEALTH_MEDICAL","description":"Emergency surgery for appendicitis at CHUM hospital. 3-day hospital stay, surgery, anesthesia, and post-op medication.","incidentDate":"2026-03-15","estimatedAmount":15000}'
  '{"policyId":5,"customerId":3,"claimType":"HOME_FIRE","description":"Kitchen fire caused by faulty electrical outlet. Damage limited to kitchen - cabinets, appliances, ceiling, and smoke damage throughout unit.","incidentDate":"2026-05-05","estimatedAmount":65000}'
  '{"policyId":6,"customerId":4,"claimType":"AUTO_COLLISION","description":"Multi-vehicle accident on Autoroute 15 during freezing rain. Front-end collision. Airbags deployed. Vehicle likely total loss.","incidentDate":"2026-01-18","estimatedAmount":28000}'
  '{"policyId":7,"customerId":4,"claimType":"LIABILITY","description":"Customer slipped on wet floor in warehouse. Suffered broken wrist and concussion. Customer has retained legal counsel.","incidentDate":"2026-02-20","estimatedAmount":120000}'
  '{"policyId":8,"customerId":5,"claimType":"HOME_WEATHER","description":"Severe windstorm damage to rental property roof. Multiple shingles torn off, water ingress into top floor unit. Tenants relocated.","incidentDate":"2026-06-01","estimatedAmount":18500}'
  '{"policyId":9,"customerId":6,"claimType":"AUTO_COLLISION","description":"Tesla hit by delivery truck while parked on Avenue du Parc. Driver side doors crushed. Dashcam footage available.","incidentDate":"2026-06-15","estimatedAmount":12000}'
  '{"policyId":10,"customerId":7,"claimType":"HEALTH_MEDICAL","description":"Medical emergency during trip to Paris. Hospitalized for 5 days with pneumonia. Travel insurance claim for medical expenses.","incidentDate":"2026-04-22","estimatedAmount":35000}'
  '{"policyId":12,"customerId":8,"claimType":"AUTO_COLLISION","description":"Deer collision on Route 117 near Mont-Tremblant. Front bumper, hood, windshield damaged. Wildlife collision report filed.","incidentDate":"2026-05-30","estimatedAmount":9200}'
  '{"policyId":2,"customerId":1,"claimType":"HOME_THEFT","description":"Break-in while on vacation. Electronics stolen including TV, laptop, gaming console. Rear window broken for entry.","incidentDate":"2026-06-20","estimatedAmount":7500}'
)
for cl in "${CLAIMS[@]}"; do
  curl -s -X POST "$BASE_URL/api/claims" -H "Content-Type: application/json" -H "$AUTH" -d "$cl" > /dev/null
done

# Update statuses
echo "Updating claim statuses..."
curl -s -X PATCH "$BASE_URL/api/claims/1/status?status=UNDER_REVIEW" -H "$AUTH" > /dev/null
curl -s -X PATCH "$BASE_URL/api/claims/2/status?status=ADJUSTER_ASSIGNED" -H "$AUTH" > /dev/null
curl -s -X PATCH "$BASE_URL/api/claims/3/status?status=INVESTIGATION" -H "$AUTH" > /dev/null
curl -s -X PATCH "$BASE_URL/api/claims/4/status?status=APPROVED" -H "$AUTH" > /dev/null
curl -s -X PATCH "$BASE_URL/api/claims/5/status?status=UNDER_REVIEW" -H "$AUTH" > /dev/null
curl -s -X PATCH "$BASE_URL/api/claims/6/status?status=APPROVED" -H "$AUTH" > /dev/null
curl -s -X PATCH "$BASE_URL/api/claims/7/status?status=FLAGGED_FRAUD" -H "$AUTH" > /dev/null
curl -s -X PATCH "$BASE_URL/api/claims/8/status?status=SETTLED" -H "$AUTH" > /dev/null
curl -s -X PATCH "$BASE_URL/api/claims/10/status?status=DENIED" -H "$AUTH" > /dev/null
curl -s -X PATCH "$BASE_URL/api/claims/11/status?status=ADJUSTER_ASSIGNED" -H "$AUTH" > /dev/null

echo ""
echo "========================================="
echo "  Data seeded successfully!"
echo "========================================="
echo ""
echo "  Login credentials:"
echo "    Admin:      admin@claimsplatform.com / password123"
echo "    Adjuster:   amelie@claimsplatform.com / password123"
echo "    Underwriter: luc@claimsplatform.com / password123"
echo ""
echo "  Data created:"
echo "    - 8 customers"
echo "    - 12 policies"
echo "    - 12 claims (various statuses)"
echo "========================================="
