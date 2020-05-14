# Coupon Management System
Rest API 기반 쿠폰 관리 시스템입니다.
* 지원 기능
  * 쿠폰 생성
  * 사용자에게 쿠폰 지급
  * 쿠폰 검색/조회
  * 쿠폰 사용/사용 취소
  * 일자 별 만료된 쿠폰 조회
  * 만료 예정(3일 전)인 쿠폰 안내 메시지 발송
    (TODO: 현재는 console에 출력하는 정도로 구현함, 추후 실제 메시지 발송 시스템에 메시지 전달 - 시스템 스펙에 따라, API 호출 또는 메시지 큐에 메시지 발행 등을 사용할 수 있음)

## Environment
* Spring Boot 2
* Kotlin
* MySQL (Docker로 별도 실행)
* mockk: https://github.com/mockk/mockk

## Strategy
* TO-BE-FILLED

## Build & Run
1. MySQL 컨테이너 실행
  ** TO-BE-FILLED
2. Coupon App 실행
  ** `$ ./gradlew bootRun` 

## DB Setting
```sql
CREATE DATABASE coupon;
CREATE USER 'user'@'localhost' IDENTIFIED BY 'qwe123';
GRANT ALL PRIVILEGES ON coupon.* TO 'user'@'localhost';
```
 