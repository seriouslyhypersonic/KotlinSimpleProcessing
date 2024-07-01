package com.seriouslyhypersonic.kspforall.demo.casedetection

import com.seriouslyhypersonic.annotations.CaseDetection

@CaseDetection
enum class Direction {
    Up, Down, Left, Right
}

@CaseDetection
enum class TextAlignment {
    FLUSH_LEFT, CENTER_ALIGNED, FLUSH_RIGHT, JUSTIFIED
}

@CaseDetection
sealed class Device(val brand: String) {
    class Laptop(brand: String) : Device(brand)
    class Smartphone(brand: String) : Device(brand)
    class Tablet(brand: String) : Device(brand)
}

@CaseDetection
sealed interface Vehicle {
    val powertrain: String

    class Bike(override val powertrain: String) : Vehicle
    data object Bycicle : Vehicle {
        override val powertrain = "Leg-power"
    }
}

@Suppress("unused", "FunctionName")
private fun `Case Detection Demo`() {
    val up = Direction.Up
    require(up.isUp)

    val centerAligned = TextAlignment.CENTER_ALIGNED
    require(centerAligned.isCenterAligned)

    val macBookPro = Device.Laptop(brand = "Apple")
    require(macBookPro.isLaptop && macBookPro.brand == "Apple")

    val fatBoy = Vehicle.Bike(powertrain = "Milwaukee-Eight engine")
    require(fatBoy.isBike)

    require(Vehicle.Bycicle.isBycicle)
}
