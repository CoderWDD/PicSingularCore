## 介绍

- 这是一个由`Kotlin` 编写的基于 `SpringBoot` 框架的图片共享平台的后端项目，目前主要为 `PicSingular` 提供服务
- 项目通过 `SpringSecurity` 进行鉴权认证，主要分为两个端，三种角色，分别是：
  - 后端管理：
    - 管理员
    - 超级管理员
  - 交互APP：
    - 用户

## 技术点

- SpringBoot
- SpringSecurity
- Spring Data JPA
- Kotlin
- Redis
- Docker

## 部署流程

- docker-compose:

  ```dockerfile
  version: "3"
  services:
    mysqldb:
      container_name: PicSingularCore_mysqldb
      image: mysql:8.0.27
      ports:
        - "${MYSQL_PORT}:3306"
      command: [
        '--character-set-server=utf8mb4',
        '--collation-server=utf8mb4_unicode_ci',
        '--default-time-zone=+8:00'
      ]
      restart: always
      environment:
        - MYSQL_ROOT_PASSWORD=${MYSQL_PASSWORD}
        - MYSQL_DATABASE=PicSingularDatabase
  
    redis:
      container_name: PicSingularDatabase_Redis
      image: redis:latest
      restart: always
      ports:
        - "${REDIS_PORT}:6379"
      environment:
        - REDIS_PORT=6379
        - CONNECT_TIMEOUT=5000
        - MAX_IDLE=10
        - MAX_WAIT=-1
        - MIN_IDLE=5
  
    PicSingular:
      container_name: PicSingularCore
      image: coderwdd/pic_singular_core:latest
      ports:
        - "${SERVER_PORT}:8080"
      restart: always
      depends_on:
        - mysqldb
        - redis
      links:
        - "mysqldb:mysqldb"
        - "redis:redis"
      env_file:
        pic_singular_core.env
      environment:
        - TZ=Asia/Shanghai
  ```

- pic_singular_core.env:

  ```yaml
  MYSQL_HOST=HostIp
  MYSQL_PORT=3306
  MYSQL_USERNAME=root
  MYSQL_PASSWORD=123456
  REDIS_HOST=HostIp
  REDIS_PORT=6379
  SERVER_PORT=8806

## TODO

- 