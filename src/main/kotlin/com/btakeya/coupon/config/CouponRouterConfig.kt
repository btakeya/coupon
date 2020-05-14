package com.btakeya.coupon.config

import com.btakeya.coupon.router.CouponRouterHandler
import com.btakeya.coupon.router.CouponRouterHandler.CouponIssueParam
import com.btakeya.coupon.router.HelloHandler
import javassist.NotFoundException
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.BodyExtractors.toMono
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Component
class CouponRouterConfig : WebFluxConfigurer {

    @Bean
    fun route(helloHandler: HelloHandler, couponHandler: CouponRouterHandler): RouterFunction<ServerResponse> = router {
        "/".nest {
            GET("/hello") {
                helloHandler.hello()
            }
        }
        "/api".nest {
            "/coupon".nest {
                GET("/hello") {
                    couponHandler.hello()
                }
                POST("/issue") { request ->
                    request.body(toMono(CouponIssueParam::class.java))
                        .flatMap { couponHandler.issue(it) }
                }
            }
        }
    }
        .filter { request, next ->
            try {
                next.handle(request)
            } catch (ex: Exception) {
                when (ex) {
                    is NotFoundException -> ServerResponse.notFound().build()
                    else -> ServerResponse.status(INTERNAL_SERVER_ERROR).build()
                }
            }
        }
}
