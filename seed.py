import urllib.request
import json
import random

BASE_URL = "http://localhost:8080/api"

def post_json(url, data):
    req = urllib.request.Request(url)
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    jsondata = json.dumps(data)
    jsondataasbytes = jsondata.encode('utf-8')
    req.add_header('Content-Length', len(jsondataasbytes))
    try:
        response = urllib.request.urlopen(req, jsondataasbytes)
        res_body = response.read()
        if res_body:
            return json.loads(res_body.decode('utf-8'))
        return {}
    except urllib.error.HTTPError as e:
        print(f"HTTP Error: {e.code} - {e.read().decode()}")
        return None
    except Exception as e:
        print(f"Error: {e}")
        return None

users_data = [
    {"userName": "John Doe", "userAddress": "123 Main St, NY", "userAadharInfo": "123456789012", "userPanInfo": "ABCDE1234F"},
    {"userName": "Jane Smith", "userAddress": "456 Market St, SF", "userAadharInfo": "234567890123", "userPanInfo": "FGHIJ5678K"},
    {"userName": "Bob Johnson", "userAddress": "789 Broad St, TX", "userAadharInfo": "345678901234", "userPanInfo": "KLMNO9012P"},
    {"userName": "Alice Davis", "userAddress": "321 Oak St, FL", "userAadharInfo": "456789012345", "userPanInfo": "QRSTU3456V"},
    {"userName": "Charlie Brown", "userAddress": "654 Pine St, WA", "userAadharInfo": "567890123456", "userPanInfo": "VWXYZ7890A"}
]

lenders_data = [
    {"lenderName": "Global Finance", "lenderContact": "contact@globalfinance.com", "lenderDetails": "International Tier-1 Bank"},
    {"lenderName": "Apex Lending", "lenderContact": "support@apexlending.net", "lenderDetails": "Domestic high-risk lender"},
    {"lenderName": "Horizon Credit", "lenderContact": "loans@horizoncredit.org", "lenderDetails": "Credit union focusing on low rates"},
    {"lenderName": "Capital One Funding", "lenderContact": "hello@capitalonefunding.com", "lenderDetails": "Corporate financial services"},
    {"lenderName": "Nexus Financial", "lenderContact": "investors@nexusfin.com", "lenderDetails": "Alternative and peer-to-peer lending"},
    {"lenderName": "Summit Trust", "lenderContact": "contact@summittrust.com", "lenderDetails": "Regional banking and trusts"},
    {"lenderName": "Vanguard Lending", "lenderContact": "support@vanguardlending.net", "lenderDetails": "Investment focused lending"},
    {"lenderName": "Liberty Capital", "lenderContact": "info@libertycap.org", "lenderDetails": "Fast approval personal loans"},
    {"lenderName": "Pioneer Finance", "lenderContact": "loans@pioneerfinance.com", "lenderDetails": "Merchant cash advances and loans"},
    {"lenderName": "Starlight Credit", "lenderContact": "help@starlightcredit.com", "lenderDetails": "Digital-first neo bank"}
]

# Generate 10 personal, 10 merchant loans to be randomly assigned
personal_profiles = [
    {"typeOfLoan": "PERSONAL", "loanAmountMin": 5000.0, "loanAmountMax": 25000.0, "loanInterestMin": 7.0, "loanInterestMax": 12.0, "loanTimeMin": 6, "loanTimeMax": 24},
    {"typeOfLoan": "PERSONAL", "loanAmountMin": 25000.0, "loanAmountMax": 100000.0, "loanInterestMin": 5.5, "loanInterestMax": 9.5, "loanTimeMin": 12, "loanTimeMax": 60},
    {"typeOfLoan": "PERSONAL", "loanAmountMin": 1000.0, "loanAmountMax": 10000.0, "loanInterestMin": 12.0, "loanInterestMax": 20.0, "loanTimeMin": 1, "loanTimeMax": 12},
    {"typeOfLoan": "PERSONAL", "loanAmountMin": 10000.0, "loanAmountMax": 40000.0, "loanInterestMin": 9.0, "loanInterestMax": 15.0, "loanTimeMin": 12, "loanTimeMax": 48},
    {"typeOfLoan": "PERSONAL", "loanAmountMin": 2000.0, "loanAmountMax": 30000.0, "loanInterestMin": 4.0, "loanInterestMax": 8.0, "loanTimeMin": 12, "loanTimeMax": 36},
    {"typeOfLoan": "PERSONAL", "loanAmountMin": 30000.0, "loanAmountMax": 75000.0, "loanInterestMin": 5.0, "loanInterestMax": 7.5, "loanTimeMin": 36, "loanTimeMax": 84},
    {"typeOfLoan": "PERSONAL", "loanAmountMin": 5000.0, "loanAmountMax": 50000.0, "loanInterestMin": 8.0, "loanInterestMax": 16.0, "loanTimeMin": 6, "loanTimeMax": 48},
    {"typeOfLoan": "PERSONAL", "loanAmountMin": 50000.0, "loanAmountMax": 150000.0, "loanInterestMin": 6.0, "loanInterestMax": 10.0, "loanTimeMin": 24, "loanTimeMax": 60},
    {"typeOfLoan": "PERSONAL", "loanAmountMin": 1000.0, "loanAmountMax": 15000.0, "loanInterestMin": 10.0, "loanInterestMax": 25.0, "loanTimeMin": 3, "loanTimeMax": 24},
    {"typeOfLoan": "PERSONAL", "loanAmountMin": 15000.0, "loanAmountMax": 60000.0, "loanInterestMin": 7.0, "loanInterestMax": 14.0, "loanTimeMin": 12, "loanTimeMax": 60}
]

merchant_profiles = [
    {"typeOfLoan": "MERCHANT", "loanAmountMin": 10000.0, "loanAmountMax": 50000.0, "loanInterestMin": 8.0, "loanInterestMax": 14.0, "loanTimeMin": 3, "loanTimeMax": 12},
    {"typeOfLoan": "MERCHANT", "loanAmountMin": 50000.0, "loanAmountMax": 500000.0, "loanInterestMin": 6.0, "loanInterestMax": 10.0, "loanTimeMin": 6, "loanTimeMax": 36},
    {"typeOfLoan": "MERCHANT", "loanAmountMin": 5000.0, "loanAmountMax": 20000.0, "loanInterestMin": 10.0, "loanInterestMax": 18.0, "loanTimeMin": 1, "loanTimeMax": 6},
    {"typeOfLoan": "MERCHANT", "loanAmountMin": 100000.0, "loanAmountMax": 1000000.0, "loanInterestMin": 5.0, "loanInterestMax": 8.5, "loanTimeMin": 12, "loanTimeMax": 60},
    {"typeOfLoan": "MERCHANT", "loanAmountMin": 15000.0, "loanAmountMax": 75000.0, "loanInterestMin": 6.5, "loanInterestMax": 11.0, "loanTimeMin": 12, "loanTimeMax": 48},
    {"typeOfLoan": "MERCHANT", "loanAmountMin": 75000.0, "loanAmountMax": 300000.0, "loanInterestMin": 5.5, "loanInterestMax": 9.0, "loanTimeMin": 24, "loanTimeMax": 72},
    {"typeOfLoan": "MERCHANT", "loanAmountMin": 20000.0, "loanAmountMax": 100000.0, "loanInterestMin": 7.5, "loanInterestMax": 12.5, "loanTimeMin": 3, "loanTimeMax": 18},
    {"typeOfLoan": "MERCHANT", "loanAmountMin": 100000.0, "loanAmountMax": 2000000.0, "loanInterestMin": 4.5, "loanInterestMax": 7.0, "loanTimeMin": 36, "loanTimeMax": 120},
    {"typeOfLoan": "MERCHANT", "loanAmountMin": 5000.0, "loanAmountMax": 30000.0, "loanInterestMin": 9.0, "loanInterestMax": 15.0, "loanTimeMin": 2, "loanTimeMax": 12},
    {"typeOfLoan": "MERCHANT", "loanAmountMin": 30000.0, "loanAmountMax": 250000.0, "loanInterestMin": 6.5, "loanInterestMax": 11.5, "loanTimeMin": 12, "loanTimeMax": 60}
]

print("Seeding database...")

# Create Users
for user in users_data:
    response = post_json(f"{BASE_URL}/users", user)
    if response and 'userId' in response:
        print(f"Created User: {user['userName']} with ID: {response['userId']}")
    else:
        print(f"Error creating user {user['userName']}")

# Create Lenders and Loans
for lender in lenders_data:
    response = post_json(f"{BASE_URL}/lenders", lender)
    if response and 'lenderId' in response:
        lender_id = response['lenderId']
        print(f"Created Lender: {lender['lenderName']} with ID: {lender_id}")
        
        # Pick 2 random personal and 2 random merchant loans
        p_loans = random.sample(personal_profiles, 2)
        m_loans = random.sample(merchant_profiles, 2)
        
        for loan in p_loans + m_loans:
            loan["lenderId"] = lender_id
            lr = post_json(f"{BASE_URL}/loans", loan)
            if lr and 'loanId' in lr:
                print(f"  -> Created {loan['typeOfLoan']} Loan: ID {lr['loanId']}")
            else:
                print("  -> Error creating loan")
    else:
        print(f"Error creating lender {lender['lenderName']}")

print("Seeding complete!")
