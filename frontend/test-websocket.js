import SockJS from 'sockjs-client';

// 使用获取到的JWT token
const token = 'eyJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOjcyLCJ1c2VybmFtZSI6ImFkbWluIiwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NDkwOTU0MjAsImV4cCI6MTc0OTE4MTgyMH0.or_cgXkzEuOjMOKwBPtvD1ZsseXwuTRWS2zuOQaFz30AmYG83Pfz_2ztEzb2S30J';

// 构建WebSocket URL
const wsUrl = `http://localhost:8080/ws?token=${encodeURIComponent(token)}`;

console.log('正在连接到:', wsUrl);

// 创建SockJS连接
const sock = new SockJS(wsUrl);

sock.onopen = function() {
    console.log('✓ WebSocket连接成功!');
    
    // 发送测试消息
    const testMessage = {
        type: "LOGIN_NOTIFICATION",
        action: "HEARTBEAT",
        data: {
            userId: "72",
            timestamp: new Date().getTime()
        }
    };
    
    console.log('发送测试消息:', JSON.stringify(testMessage));
    sock.send(JSON.stringify(testMessage));
};

sock.onmessage = function(e) {
    console.log('← 收到消息:', e.data);
};

sock.onclose = function(e) {
    console.log('✗ 连接关闭:', e.code, e.reason);
    process.exit(0);
};

sock.onerror = function(e) {
    console.log('✗ 连接错误:', e);
    process.exit(1);
};

// 5秒后自动断开
setTimeout(() => {
    console.log('5秒后自动断开连接...');
    sock.close();
}, 5000); 