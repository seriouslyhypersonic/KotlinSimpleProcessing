package com.seriouslyhypersonic.processor.casedetection

import com.seriouslyhypersonic.processor.ktx.kspGeneratedSources
import com.seriouslyhypersonic.processor.tools.assertKotlinSource
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCompilerApi::class)
class CaseDetectionProcessorTest {
    @Test
    fun `Test enum class with Pascal cases`() {
        val enumSource = SourceFile.kotlin(
            name = "Color.kt",
            contents = """
                package com.test.pascal.cases
                import com.seriouslyhypersonic.annotations.CaseDetection

                @CaseDetection
                enum class Color { 
                    Red, Green, Blue 
                }
            """
        )

        val result = compile(enumSource)
        assertEquals(expected = KotlinCompilation.ExitCode.OK, actual = result.exitCode)

        val file = requireNotNull(result.kspGeneratedSources.first())
        assertEquals(expected = "ColorCaseDetection.kt", actual = file.name)

        assertKotlinSource(
            file = file,
            expected = """
                package com.test.pascal.cases

                import kotlin.Boolean
                
                /**
                 * Returns `true` if this [Color] is [Color.Red], `false` otherwise.
                 */
                public val Color.isRed: Boolean
                  get() = this == Color.Red
                
                /**
                 * Returns `true` if this [Color] is [Color.Green], `false` otherwise.
                 */
                public val Color.isGreen: Boolean
                  get() = this == Color.Green
                
                /**
                 * Returns `true` if this [Color] is [Color.Blue], `false` otherwise.
                 */
                public val Color.isBlue: Boolean
                  get() = this == Color.Blue

            """
        )
    }

    @Test
    fun `Test enum class with snake cases`() {
        val enumSource = SourceFile.kotlin(
            name = "TextAlignment.kt",
            contents = """
                package com.test.snake.cases
                import com.seriouslyhypersonic.annotations.CaseDetection

                @CaseDetection
                enum class TextAlignment {
                    FLUSH_LEFT, CENTER_ALIGNED, FLUSH_RIGHT, JUSTIFIED
                }
            """
        )

        val result = compile(enumSource)
        assertEquals(expected = KotlinCompilation.ExitCode.OK, actual = result.exitCode)

        val file = requireNotNull(result.kspGeneratedSources.first())
        assertEquals(expected = "TextAlignmentCaseDetection.kt", actual = file.name)

        assertKotlinSource(
            file = file,
            expected = """
                package com.test.snake.cases

                import kotlin.Boolean
                
                /**
                 * Returns `true` if this [TextAlignment] is [TextAlignment.FLUSH_LEFT], `false` otherwise.
                 */
                public val TextAlignment.isFlushLeft: Boolean
                  get() = this == TextAlignment.FLUSH_LEFT
                
                /**
                 * Returns `true` if this [TextAlignment] is [TextAlignment.CENTER_ALIGNED], `false` otherwise.
                 */
                public val TextAlignment.isCenterAligned: Boolean
                  get() = this == TextAlignment.CENTER_ALIGNED
                
                /**
                 * Returns `true` if this [TextAlignment] is [TextAlignment.FLUSH_RIGHT], `false` otherwise.
                 */
                public val TextAlignment.isFlushRight: Boolean
                  get() = this == TextAlignment.FLUSH_RIGHT
                
                /**
                 * Returns `true` if this [TextAlignment] is [TextAlignment.JUSTIFIED], `false` otherwise.
                 */
                public val TextAlignment.isJustified: Boolean
                  get() = this == TextAlignment.JUSTIFIED

            """
        )
    }

    @Test
    fun `Test sealed class`() {
        val enumSource = SourceFile.kotlin(
            name = "Device.kt",
            contents = """
                package com.test.sealed.classes
                import com.seriouslyhypersonic.annotations.CaseDetection

                @CaseDetection
                sealed class Device(val brand: String) {
                    class Laptop(brand: String) : Device(brand)
                    class Smartphone(brand: String) : Device(brand)
                    class Tablet(brand: String) : Device(brand)
                }
            """
        )

        val result = compile(enumSource)
        assertEquals(expected = KotlinCompilation.ExitCode.OK, actual = result.exitCode)

        val file = requireNotNull(result.kspGeneratedSources.first())
        assertEquals(expected = "DeviceCaseDetection.kt", actual = file.name)

        assertKotlinSource(
            file = file,
            expected = """
                package com.test.`sealed`.classes
                
                import kotlin.Boolean
                
                /**
                 * Returns `true` if this [Device] is [Device.Laptop], `false` otherwise.
                 */
                public val Device.isLaptop: Boolean
                  get() = this is Device.Laptop
                
                /**
                 * Returns `true` if this [Device] is [Device.Smartphone], `false` otherwise.
                 */
                public val Device.isSmartphone: Boolean
                  get() = this is Device.Smartphone
                
                /**
                 * Returns `true` if this [Device] is [Device.Tablet], `false` otherwise.
                 */
                public val Device.isTablet: Boolean
                  get() = this is Device.Tablet

            """
        )
    }

    @Test
    fun `Test sealed interface`() {
        val enumSource = SourceFile.kotlin(
            name = "Vehicle.kt",
            contents = """
                package com.test.sealed.classes
                import com.seriouslyhypersonic.annotations.CaseDetection

                @CaseDetection
                sealed interface Vehicle {
                    val powertrain: String
                
                    class Bike(override val powertrain: String) : Vehicle
                    data object Bycicle : Vehicle {
                        override val powertrain = "Leg-power"
                    }
                }
            """
        )

        val result = compile(enumSource)
        assertEquals(expected = KotlinCompilation.ExitCode.OK, actual = result.exitCode)

        val file = requireNotNull(result.kspGeneratedSources.first())
        assertEquals(expected = "VehicleCaseDetection.kt", actual = file.name)

        assertKotlinSource(
            file = file,
            expected = """
                package com.test.`sealed`.classes
                
                import kotlin.Boolean
                
                /**
                 * Returns `true` if this [Vehicle] is [Vehicle.Bike], `false` otherwise.
                 */
                public val Vehicle.isBike: Boolean
                  get() = this is Vehicle.Bike
                
                /**
                 * Returns `true` if this [Vehicle] is [Vehicle.Bycicle], `false` otherwise.
                 */
                public val Vehicle.isBycicle: Boolean
                  get() = this is Vehicle.Bycicle

            """
        )
    }

    private fun compile(source: SourceFile): KotlinCompilation.Result = KotlinCompilation()
        .apply {
            sources = listOf(source)
            symbolProcessorProviders = listOf(CaseDetectionProcessorProvider())
            inheritClassPath = true
        }
        .compile()
}
