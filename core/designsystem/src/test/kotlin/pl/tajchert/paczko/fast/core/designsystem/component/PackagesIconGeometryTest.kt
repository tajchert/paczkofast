package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The animated Packages icon interpolates every vertex between a closed and an
 * open position. These tests pin the two endpoints to the exact design-frame
 * coordinates so the morph stays faithful to the source SVGs, and check that the
 * strap only shows while closed.
 */
class PackagesIconGeometryTest {

    @Test
    fun `closed frame reproduces the closed design vertices`() {
        val g = packagesIconGeometry(openFraction = 0f)

        assertPoints(
            listOf(
                Offset(3.5f, 9f), Offset(3.5f, 21f), Offset(16f, 27.5f),
                Offset(28.5f, 21f), Offset(28.5f, 9f), Offset(16f, 15.5f),
            ),
            g.body,
        )
        assertPoints(listOf(Offset(16f, 15.5f), Offset(16f, 27.5f)), g.centerEdge)
        assertPoints(listOf(Offset(3.5f, 9f), Offset(16f, 15.5f), Offset(28.5f, 9f)), g.openingV)
        // Flaps collapse onto the closed silhouette apex edges.
        assertPoints(
            listOf(Offset(3.5f, 9f), Offset(3.5f, 9f), Offset(16f, 2.5f), Offset(16f, 2.5f)),
            g.leftFlap,
        )
        assertPoints(
            listOf(Offset(28.5f, 9f), Offset(28.5f, 9f), Offset(16f, 2.5f), Offset(16f, 2.5f)),
            g.rightFlap,
        )
        assertEquals(1f, g.strapAlpha, EPS)
    }

    @Test
    fun `open frame opens the top while the base stays pinned`() {
        val g = packagesIconGeometry(openFraction = 1f)

        assertPoints(
            listOf(
                Offset(3.5f, 13.5f), Offset(3.5f, 21f), Offset(16f, 27.5f),
                Offset(28.5f, 21f), Offset(28.5f, 13.5f), Offset(16f, 20f),
            ),
            g.body,
        )
        assertPoints(listOf(Offset(16f, 20f), Offset(16f, 27.5f)), g.centerEdge)
        assertPoints(listOf(Offset(3.5f, 13.5f), Offset(16f, 7f), Offset(28.5f, 13.5f)), g.openingV)
        assertPoints(
            listOf(Offset(3.5f, 13.5f), Offset(2f, 9f), Offset(14.5f, 15.5f), Offset(16f, 20f)),
            g.leftFlap,
        )
        assertPoints(
            listOf(Offset(28.5f, 13.5f), Offset(30f, 9f), Offset(17.5f, 2.5f), Offset(16f, 7f)),
            g.rightFlap,
        )
        assertEquals(0f, g.strapAlpha, EPS)
    }

    @Test
    fun `box bottom trapezoid never moves while opening`() {
        // bBL, bBot, bBR (indices 1, 2, 3) are pinned across the whole animation.
        listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { fraction ->
            val body = packagesIconGeometry(openFraction = fraction).body
            assertPoints(
                listOf(Offset(3.5f, 21f), Offset(16f, 27.5f), Offset(28.5f, 21f)),
                listOf(body[1], body[2], body[3]),
            )
        }
    }

    @Test
    fun `half-open frame sits midway between the two frames`() {
        val g = packagesIconGeometry(openFraction = 0.5f)

        // Center-edge top point: midpoint of 15.5 (closed) and 20 (open).
        assertEquals(17.75f, g.centerEdge[0].y, EPS)
        // Opening apex lifts from 15.5 toward 7.
        assertEquals(11.25f, g.openingV[1].y, EPS)
        assertEquals(0.5f, g.strapAlpha, EPS)
    }

    @Test
    fun `fraction is clamped to the valid range`() {
        val below = packagesIconGeometry(openFraction = -1f)
        val above = packagesIconGeometry(openFraction = 2f)

        assertPoints(packagesIconGeometry(0f).body, below.body)
        assertPoints(packagesIconGeometry(1f).body, above.body)
    }

    private fun assertPoints(expected: List<Offset>, actual: List<Offset>) {
        assertEquals("point count", expected.size, actual.size)
        expected.forEachIndexed { index, point ->
            assertEquals("x[$index]", point.x, actual[index].x, EPS)
            assertEquals("y[$index]", point.y, actual[index].y, EPS)
        }
    }

    companion object {
        private const val EPS = 0.001f
    }
}
