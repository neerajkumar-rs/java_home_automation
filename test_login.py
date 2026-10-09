import requests
import json

url = "http://localhost:8081/api/auth/login"
data = {
    "username": "admin",
    "password": "admin123"
}

try:
    response = requests.post(url, json=data)
    print(f"Status Code: {response.status_code}")
    print(f"Response: {response.text}")
    
    if response.status_code == 200:
        result = response.json()
        print(f"\nLogin successful!")
        print(f"Token: {result.get('token', 'No token received')[:30]}...")
        print(f"User: {result.get('username')}")
        print(f"Is Admin: {result.get('admin')}")
        
        # Test getting devices with the token
        token = result.get('token')
        if token:
            print("\nTesting device endpoint with token...")
            headers = {"Authorization": f"Bearer {token}"}
            devices_response = requests.get("http://localhost:8081/device/s", headers=headers)
            print(f"Devices Status: {devices_response.status_code}")
            print(f"Devices Response: {devices_response.text}")
    else:
        print(f"\nLogin failed: {response.text}")
        
except Exception as e:
    print(f"Error: {e}")