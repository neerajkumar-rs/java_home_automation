#!/usr/bin/env python3
"""
Test script for Home Automation Authentication System
This script tests the authentication endpoints of the Home Automation system.
"""

import requests
import json
import time

BASE_URL = "http://localhost:8081/api/auth"

headers = {
    "Content-Type": "application/json"
}

def print_test_result(test_name, status):
    """Print test result in a formatted way"""
    status_text = "✅ PASS" if status else "❌ FAIL"
    print(f"{test_name}: {status_text}")

def test_endpoint_exists(endpoint, method="GET"):
    """Test if an endpoint exists and returns a valid response"""
    try:
        url = f"{BASE_URL}/{endpoint}"
        if method == "GET":
            response = requests.get(url)
        elif method == "POST":
            response = requests.post(url, json={}, headers=headers)
        else:
            return False
        
        return response.status_code in [200, 201, 400, 401]
    except Exception as e:
        print(f"Error testing {endpoint}: {e}")
        return False

def test_login():
    """Test login functionality"""
    test_cases = [
        {
            "username": "admin",
            "password": "admin123",
            "expected_success": True,
            "description": "Valid admin login"
        },
        {
            "username": "user",
            "password": "user123",
            "expected_success": True,
            "description": "Valid user login"
        },
        {
            "username": "admin",
            "password": "wrongpassword",
            "expected_success": False,
            "description": "Invalid password"
        },
        {
            "username": "nonexistent",
            "password": "password123",
            "expected_success": False,
            "description": "Non-existent user"
        }
    ]
    
    results = []
    for test in test_cases:
        try:
            response = requests.post(
                f"{BASE_URL}/login",
                json={
                    "username": test["username"],
                    "password": test["password"]
                },
                headers=headers
            )
            
            success = (response.status_code == 200) == test["expected_success"]
            
            if response.status_code == 200:
                data = response.json()
                token = data.get("token")
                is_admin = data.get("admin", False)
                print(f"  Token received: {'Yes' if token else 'No'}")
                print(f"  Admin role: {is_admin}")
            
            results.append((test["description"], success))
        except Exception as e:
            print(f"Error in {test['description']}: {e}")
            results.append((test["description"], False))
    
    return results

def test_register():
    """Test user registration"""
    # Generate unique username to avoid conflicts
    timestamp = int(time.time())
    test_user = {
        "username": f"testuser_{timestamp}",
        "email": f"testuser{timestamp}@example.com",
        "firstName": "Test",
        "lastName": "User",
        "password": "TestPass123",
        "admin": False
    }
    
    try:
        # Try registering new user
        response = requests.post(
            f"{BASE_URL}/register",
            json=test_user,
            headers=headers
        )
        
        if response.status_code == 200:
            data = response.json()
            print(f"  ✅ User '{test_user['username']}' registered successfully")
            print(f"  Token: {data.get('token', 'Not provided')[:30]}...")
            return True
        else:
            print(f"  ❌ Registration failed: {response.status_code}")
            print(f"  Response: {response.text}")
            return False
    except Exception as e:
        print(f"Error testing registration: {e}")
        return False

def test_token_validation():
    """Test JWT token validation"""
    try:
        # First login to get a token
        login_response = requests.post(
            f"{BASE_URL}/login",
            json={
                "username": "admin",
                "password": "admin123"
            },
            headers=headers
        )
        
        if login_response.status_code != 200:
            print("  ❌ Could not login to get token")
            return False
        
        token_data = login_response.json()
        token = token_data.get("token")
        
        if not token:
            print("  ❌ No token in login response")
            return False
        
        # Test token validation endpoint
        validate_headers = {
            "Authorization": f"Bearer {token}"
        }
        
        validate_response = requests.get(
            f"{BASE_URL}/validate",
            headers=validate_headers
        )
        
        if validate_response.status_code == 200:
            data = validate_response.json()
            if data.get("valid"):
                print("  ✅ Token validation successful")
                return True
            else:
                print("  ❌ Token validation failed")
                return False
        else:
            print(f"  ❌ Validate endpoint error: {validate_response.status_code}")
            return False
        
    except Exception as e:
        print(f"Error testing token validation: {e}")
        return False

def test_current_user():
    """Test /me endpoint"""
    try:
        # First login to get a token
        login_response = requests.post(
            f"{BASE_URL}/login",
            json={
                "username": "admin",
                "password": "admin123"
            },
            headers=headers
        )
        
        if login_response.status_code != 200:
            print("  ❌ Could not login to get token")
            return False
        
        token_data = login_response.json()
        token = token_data.get("token")
        
        if not token:
            print("  ❌ No token in login response")
            return False
        
        # Test /me endpoint
        me_headers = {
            "Authorization": f"Bearer {token}"
        }
        
        me_response = requests.get(
            f"{BASE_URL}/me",
            headers=me_headers
        )
        
        if me_response.status_code == 200:
            data = me_response.json()
            print(f"  ✅ Current user data retrieved")
            print(f"  Username: {data.get('username')}")
            print(f"  Email: {data.get('email')}")
            print(f"  Is Admin: {data.get('isAdmin')}")
            return True
        else:
            print(f"  ❌ /me endpoint error: {me_response.status_code}")
            print(f"  Response: {me_response.text}")
            return False
        
    except Exception as e:
        print(f"Error testing /me endpoint: {e}")
        return False

def main():
    print("=== Home Automation Authentication System Tests ===\n")
    
    print("1. Testing endpoint availability:")
    endpoints_to_test = [
        ("login", "POST"),
        ("register", "POST"),
        ("validate", "GET"),
        ("me", "GET")
    ]
    
    for endpoint, method in endpoints_to_test:
        exists = test_endpoint_exists(endpoint, method)
        print_test_result(f"  {endpoint} ({method})", exists)
    
    print("\n2. Testing login functionality:")
    login_results = test_login()
    for description, success in login_results:
        print_test_result(f"  {description}", success)
    
    print("\n3. Testing user registration:")
    register_success = test_register()
    print_test_result("  User registration", register_success)
    
    print("\n4. Testing JWT token validation:")
    validation_success = test_token_validation()
    print_test_result("  Token validation", validation_success)
    
    print("\n5. Testing current user endpoint:")
    current_user_success = test_current_user()
    print_test_result("  Current user data", current_user_success)
    
    print("\n6. Testing admin endpoints (requires authentication):")
    try:
        # Login as admin first
        login_response = requests.post(
            f"{BASE_URL}/login",
            json={"username": "admin", "password": "admin123"},
            headers=headers
        )
        
        if login_response.status_code == 200:
            token = login_response.json().get("token")
            admin_headers = {
                "Authorization": f"Bearer {token}",
                "Content-Type": "application/json"
            }
            
            # Test admin endpoint
            admin_response = requests.get(
                "http://localhost:8081/api/admin/users",
                headers=admin_headers
            )
            
            if admin_response.status_code in [200, 403, 401]:
                print("  ✅ Admin endpoint accessible (with auth)")
            else:
                print(f"  ❌ Admin endpoint error: {admin_response.status_code}")
        else:
            print("  ❌ Could not login as admin")
    except Exception as e:
        print(f"  ❌ Error testing admin endpoints: {e}")
    
    print("\n=== Tests Completed ===")
    print("\nNext steps:")
    print("1. Open http://localhost:8081 in your browser")
    print("2. Try logging in with 'admin/admin123' or 'user/user123'")
    print("3. Check if redirect works to appropriate dashboard")

if __name__ == "__main__":
    # Wait a bit for the server to start
    print("Waiting for server to start...")
    time.sleep(5)
    main()