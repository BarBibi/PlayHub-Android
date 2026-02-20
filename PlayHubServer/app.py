import os
from flask import Flask, request, jsonify
from pymongo import MongoClient
import certifi
from datetime import datetime
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)

# MongoDB connection
mongo_uri = os.getenv("MONGO_URI")
client = MongoClient(mongo_uri, tlsCAFile=certifi.where())
db = client['PlayHubDB']      
users_collection = db['users'] 

# -------------------- API ENDPOINTS --------------------

# Create user
@app.route('/api/users', methods=['POST'])
def create_user():
    data = request.get_json()
    
    if 'uid' not in data:
        return jsonify({"error": "UID is required"}), 400

    new_user = {
        "_id": data['uid'],           
        "email": data.get('email'),
        "password": data.get('password'),
        "nickname": data.get('nickname'),
        "birthDate": data.get('birthDate'),
        "phone": data.get('phone'),
        "gender": data.get('gender'),
        "profileImage": "",
        "favorites": [],
        "following": [],
        "followers": []
    }

    try:
        users_collection.insert_one(new_user)
        return jsonify({"message": "User created successfully"}), 201
    except Exception as e:
        print(f"CRITICAL ERROR: {e}")
        return jsonify({"error": str(e)}), 500
    
# ------------------------------------------------------------------------------------------------

# Fetch user details (GET)
@app.route('/api/users/<uid>', methods=['GET'])
def get_user(uid):
    # Search for the user in the database by the ID
    user = users_collection.find_one({"_id": uid})
    
    if user:
        # Return the user as JSON
        return jsonify(user), 200
    else:
        return jsonify({"error": "User not found"}), 404

# ------------------------------------------------------------------------------------------------

# Update user details
@app.route('/api/users/<uid>', methods=['PUT'])
def update_user(uid):
    data = request.get_json()
    
    updates = {}
    
    # List of allowed fields to update
    # We check if key exists in data AND value is not empty
    if 'nickname' in data and data['nickname']:
        updates['nickname'] = data['nickname']
        
    if 'phone' in data and data['phone']:
        updates['phone'] = data['phone']
        
    if 'birthDate' in data and data['birthDate']:
        updates['birthDate'] = data['birthDate']
        
    if 'gender' in data and data['gender']:
        updates['gender'] = data['gender']

    if 'password' in data and data['password']:
        updates['password'] = data['password']
    
    if 'profileImage' in data and data['profileImage']:
        updates['profileImage'] = data['profileImage']
    
    # If there is nothing to update, return success immediately
    if not updates:
        return jsonify({"message": "No changes detected"}), 200

    # Perform the update with $set
    try:
        users_collection.update_one({"_id": uid}, {"$set": updates})
        return jsonify({"message": "User updated successfully"}), 200
    except Exception as e:
        print(f"Error updating user: {e}")
        return jsonify({"error": str(e)}), 500

# ------------------------------------------------------------------------------------------------

# Add game to favorites
@app.route('/api/users/<uid>/favorites', methods=['POST'])
def add_favorite(uid):
    data = request.get_json()
    game_id = data.get('gameId')

    if not game_id:
        return jsonify({"error": "Game ID is required"}), 400

    try:
        # $addToSet adds the item only if it doesn't exist already
        users_collection.update_one(
            {"_id": uid},
            {"$addToSet": {"favorites": game_id}}
        )
        return jsonify({"message": "Added to favorites"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# ------------------------------------------------------------------------------------------------

# Remove game from favorites
@app.route('/api/users/<uid>/favorites', methods=['DELETE'])
def remove_favorite(uid):
    data = request.get_json()
    game_id = data.get('gameId')

    try:
        # $pull removes the item from the array
        users_collection.update_one(
            {"_id": uid},
            {"$pull": {"favorites": game_id}}
        )
        return jsonify({"message": "Removed from favorites"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# ------------------------------------------------------------------------------------------------

# Post a new comment
@app.route('/api/comments', methods=['POST'])
def add_comment():
    data = request.get_json()
    
    new_comment = {
        "gameId": data.get('gameId'),
        "userId": data.get('userId'),
        "nickname": data.get('nickname'),
        "email": data.get('email'),
        "content": data.get('content'),
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    }

    try:
        db.comments.insert_one(new_comment)
        return jsonify({"message": "Comment added"}), 201
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# ------------------------------------------------------------------------------------------------

# Get comments for a specific game (sorted from newest to oldest)
@app.route('/api/comments/<int:game_id>', methods=['GET'])
def get_comments(game_id):
    try:
        # find by gameId, sort by timestamp descending (-1 = newest to oldest)
        comments = list(db.comments.find({"gameId": game_id}).sort("timestamp", -1))
        
        for comment in comments:
            comment['_id'] = str(comment['_id']) # Convert ObjectId to string for JSON compatibility
            
        return jsonify(comments), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# ------------------------------------------------------------------------------------------------

# Search Users
@app.route('/api/users/search', methods=['GET'])
def search_users():
    query = request.args.get('q', '')
    current_uid = request.args.get('uid', '')

    if not query:
        return jsonify([]), 200

    # Search users whose nickname contains the string (Case Insensitive)
    # The $ne (Not Equal) filter ensures that the user searching for themselves is not returned
    users = list(users_collection.find({
        "nickname": {"$regex": query, "$options": "i"},
        "_id": {"$ne": current_uid}
    }))

    for user in users:
        user['_id'] = str(user['_id'])
        # Delete the password field
        if 'password' in user:
            del user['password']

    return jsonify(users), 200

# ------------------------------------------------------------------------------------------------

# Follow User
@app.route('/api/users/follow', methods=['POST'])
def follow_user():
    data = request.get_json()
    current_uid = data.get('currentUserId')
    target_uid = data.get('targetUserId')

    if not current_uid or not target_uid:
        return jsonify({"error": "Missing user IDs"}), 400

    try:
        # Add Target to my Following list
        users_collection.update_one(
            {"_id": current_uid},
            {"$addToSet": {"following": target_uid}}
        )

        # Add me to the Target Followers list
        users_collection.update_one(
            {"_id": target_uid},
            {"$addToSet": {"followers": current_uid}}
        )

        return jsonify({"message": "Followed successfully"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# ------------------------------------------------------------------------------------------------

# Unfollow User
@app.route('/api/users/unfollow', methods=['POST'])
def unfollow_user():
    data = request.get_json()
    current_uid = data.get('currentUserId')
    target_uid = data.get('targetUserId')

    try:
        # Remove from my list
        users_collection.update_one(
            {"_id": current_uid},
            {"$pull": {"following": target_uid}}
        )

        # Remove me from his list.
        users_collection.update_one(
            {"_id": target_uid},
            {"$pull": {"followers": current_uid}}
        )

        return jsonify({"message": "Unfollowed successfully"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)