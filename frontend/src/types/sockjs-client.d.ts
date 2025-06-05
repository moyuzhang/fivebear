declare module 'sockjs-client' {
  interface SockJSOptions {
    server?: string
    sessionId?: number | (() => string)
    transports?: string[]
  }

  class SockJS extends WebSocket {
    constructor(url: string, protocols?: string | string[], options?: SockJSOptions)
  }

  export = SockJS
} 