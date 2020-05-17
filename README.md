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
* mockk: https://github.com/mockk/mockk, Kotlin 환경에서의 mocking library

## Strategy
* 기본적으로 Spring Boot의 WebFlux를 사용하여 가능한 비동기로 실행할 수 있도록 구성되었습니다.
  * 현재 프로젝트 구성 중 데이터베이스 연결은 비동기 연결을 지원하지 않는 MySQL JDBC Driver(`com.mysql.cj.jdbc.Driver`)를 사용하고 있지만, `Flux.defer`와 `'CPU Core 수 ⨉ 10'개 만큼의 워커 스레드`를 사용하여 가능한 비동기처럼 동작하게 구성했습니다. 이 때문에 DB의 Connection Pool Size가 워커 스레드 수보다 많아야 합니다.
  * Spring Data R2DBC 등 비동기를 지원하는 DB Connector를 사용하면 위 고려사항을 덜 수 있을 것으로 짐작되지만, 짧은 구현시간으로 인해 익숙한 환경에서 작업하기 위해 생략했습니다.
* 쿠폰 번호는 시스템에서 유일함을 보장하기 위해 UUID를 사용했습니다. 추후 스케일 아웃 시 병목이 될 것으로 짐작이 됩니다만, 쿠폰 번호 생성 모듈을 별도로 분리하는 등으로 완화할 수 있을 것으로 생각합니다.
* 쿠폰 유효기간 만료 여부를 체크하기 위해 Spring Scheduler를 사용해, 매일 00:00:00(`@Schedule(cron = "0 0 0 * * *")`에 생성 일자로부터 3일이 지났으면서 `expired=false`인 쿠폰들을 모두 `expired=true`로 마킹합니다. 여기서 timezone 이슈가 발생할 가능성이 있습니다만, 우선은 단일 timezone을 가정합니다.

## Build & Run
1. MySQL 컨테이너 실행
  ```
  docker pull mysql
  docker run -p 3306:3306 -p 33060:33060 --name local-mysql -e MYSQL_ROOT_PASSWORD=qwe123 -d mysql
  docker start local-mysql
  ```
2. Coupon App 실행
  ** `$ ./gradlew bootRun` 

## DB Setting
```sql
CREATE DATABASE coupon;
CREATE USER 'user'@'localhost' IDENTIFIED BY 'qwe123';
GRANT ALL PRIVILEGES ON coupon.* TO 'user'@'localhost';
```
* 테이블은 `spring.jpa.hibernate.ddl-auto` 옵션을 사용하여 생성한다. (`create` 옵션 사용 시 앱 런칭 중 테이블 초기화(drop + create) 수행)

## Future Work
* 100억 개 이상의 쿠폰을 저장하기 위해서, DB Sharding을 할 필요가 있습니다. 아래와 같은 전략을 선택할 수 있을 것으로 기대됩니다:
  1. Coupon code 기반: 쿠폰의 code의 값이 128bits임을 이용하여, code 앞 Xbits를 취해 대상 DB를 sharding 합니다. 예를 들어, X=3일 때 `1111...`인 코드와 `1112...`인 코드는 같은 DB 인스턴스를 대상으로 명령을 수행하지만, `1234...`인 코드는 다른 DB 인스턴스를 대상으로 명령을 수행하게 됩니다. 이렇게 되면 여러 DB 인스턴스에 레코드를 분산할 수 있지만, DB 인스턴스 간 분배가 균등하지 않을 수 있고 스케일 인/아웃 시 재분배 비용이 들어갈 수 있습니다.
  2. Coupon Hash-value 기반: 쿠폰의 hash 값에 따라 대상 DB 인스턴스를 정합니다. DB 인스턴스 분배가 가능한 균등하게 발생하도록 hash function을 정하는 것이 중요합니다. 마찬가지로 스케일 인/아웃 시 재분배 비용이 들어갈 수 있지만, Consistent hashing을 이용하여 최소화 할 수 있습니다.
  * 현재는 Coupon code 기반의 연산만 지원하므로 위 방식으로 sharding 하는 것에 무리가 없을 것 같지만, 특정 사용자에게 할당된 쿠폰을 가져오는 등의 다른 기반 연산이 필요해지면 이슈가 발생할 수 있습니다.
* 우선 각 모듈의 일부 테스트만 작성하여, 유닛 테스트를 수행할 수 있다는 정도에서만 검증을 수행했습니다. 필요 시 테스트를 작성하지 않은 연산들을 대상으로도 검증을 수행해야 합니다.
* end-to-end 테스트가 있으면 더 완성도 있는 검증을 할 수 있을 것으로 기대되나, 환경을 구성하는데 시간이 많이 들 것으로 우려되어 생략했습니다.
* Kotlin에서 타입 추론의 한계 때문에 일부 테스트가 실행되지 않습니다. 정확히는, 내부 `assert`는 모두 통과하는데 Spring WebTestClient 모듈의 `consumeWith`에서 NPE가 발생합니다. Kotlin의 type inference 문제로 보임 (https://youtrack.jetbrains.com/issue/KT-5464#comment=27-2262874)
