# 登录流程（Mermaid，Word/Typora/部分 IDE 可直接渲染）

```mermaid
flowchart LR
    A[GET /login] --> B[展示 login.html]
    B --> C[POST /login]
    C --> D[Spring Security]
    D --> E[查库 UserDetailsServiceImpl]
    E --> F{用户存在且启用}
    F -->|否| G[/login?error 或 disabled]
    F -->|是| H[FlexiblePasswordEncoder 验密]
    H --> I{密码正确}
    I -->|否| G
    I -->|是| J[写入 Session]
    J --> K[跳转首页 /]
```
