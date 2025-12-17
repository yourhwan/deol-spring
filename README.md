# Deol - Music Streaming Web App

Deol은 음원 스트리밍과 아티스트 음원 업로드를 중심으로 만든 웹 서비스입니다.  
사용자는 트랙 재생/플레이리스트 관리/실시간 차트를 이용할 수 있고, 아티스트는 앨범 단위로 음원을 업로드할 수 있습니다.


## My Role
- 기획부터 개발/배포까지 전 과정을 1인으로 진행했습니다.
- Frontend(React) / Backend(Spring Boot) / AWS 배포까지 구현했습니다.


## Tech Stack
- Frontend: React, React Router, Axios, CSS
- Backend: Spring Boot (Java 17), Spring Security (JWT), JPA (Hibernate)
- DB: MariaDB (RDS)
- Infra/DevOps: AWS EC2 (Ubuntu), Nginx (Reverse Proxy/HTTPS)
- Storage: AWS S3 (이미지/음원)


## Repositories
- Backend: https://github.com/yourhwan/deol-spring
- Frontend: https://github.com/yourhwan/deol-react


## Test Account
- 일반회원: ID: test02
           PW: qwer1234$
- 아티스트 회원: ID: test10
          PW: qwer1234$


## Key Features
### 일반회원
- 회원가입 / 로그인 (JWT)
- 플레이리스트 생성/수정/삭제, 트랙 추가/삭제
- 현재 재생목록(Queue) 기반 재생
- 실시간 Top 100 차트 (재생 수 기반)


### 아티스트
- 앨범 단위 업로드 (앨범 커버 + 트랙 파일)
- 아티스트 상세: 최신 발매 / 인기곡 / 전체 앨범



