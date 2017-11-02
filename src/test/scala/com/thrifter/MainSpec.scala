package com.thrifter

import org.scalatest.WordSpec

class MainSpec extends WordSpec {

    "A thrifter" when {
        "executed" should {
            Main.main(Array("src/test/resources/calculator.thrift"))
        }
    }
}

