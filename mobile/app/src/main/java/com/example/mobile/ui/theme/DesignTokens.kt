package com.example.mobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Standardized design tokens for Habit Pet UI.
 * Single source of truth for spacing, corner radius, elevation, and alpha values.
 */
object DesignTokens {
    // ── Spacing Scale ──────────────────────────────────────────────
    val space1 = 1.dp
    val space2 = 2.dp
    val space4 = 4.dp
    val space6 = 6.dp
    val space8 = 8.dp
    val space10 = 10.dp
    val space12 = 12.dp
    val space14 = 14.dp
    val space16 = 16.dp
    val space18 = 18.dp
    val space20 = 20.dp
    val space24 = 24.dp
    val space28 = 28.dp
    val space32 = 32.dp
    val space40 = 40.dp
    val space48 = 48.dp
    val space56 = 56.dp
    val space64 = 64.dp
    val space72 = 72.dp
    val space96 = 96.dp

    // ── Corner Radius Scale ────────────────────────────────────────
    val radiusXs = 4.dp
    val radiusSm = 8.dp
    val radiusMd = 12.dp
    val radiusLg = 14.dp
    val radiusXl = 16.dp
    val radius2xl = 18.dp
    val radius3xl = 20.dp
    val radius4xl = 22.dp
    val radius5xl = 24.dp
    val radius6xl = 26.dp
    val radiusFull = 999.dp

    // ── Elevation Scale ────────────────────────────────────────────
    val elevationNone = 0.dp
    val elevationXs = 0.5.dp
    val elevationSm = 1.dp
    val elevationMd = 2.dp
    val elevationLg = 4.dp
    val elevationXl = 6.dp
    val elevation2xl = 8.dp

    // ── Stroke Width Scale ─────────────────────────────────────────
    val strokeThin = 1.dp
    val strokeMedium = 1.5.dp
    val strokeThick = 2.dp

    // ── Alpha Scale ────────────────────────────────────────────────
    val alpha0 = 0.0f
    val alpha4 = 0.04f
    val alpha6 = 0.06f
    val alpha8 = 0.08f
    val alpha10 = 0.10f
    val alpha12 = 0.12f
    val alpha16 = 0.16f
    val alpha20 = 0.20f
    val alpha22 = 0.22f
    val alpha24 = 0.24f
    val alpha25 = 0.25f
    val alpha28 = 0.28f
    val alpha30 = 0.30f
    val alpha32 = 0.32f
    val alpha34 = 0.34f
    val alpha35 = 0.35f
    val alpha40 = 0.40f
    val alpha42 = 0.42f
    val alpha48 = 0.48f
    val alpha50 = 0.50f
    val alpha55 = 0.55f
    val alpha58 = 0.58f
    val alpha60 = 0.60f
    val alpha62 = 0.62f
    val alpha65 = 0.65f
    val alpha70 = 0.70f
    val alpha72 = 0.72f
    val alpha75 = 0.75f
    val alpha80 = 0.80f
    val alpha82 = 0.82f
    val alpha85 = 0.85f
    val alpha86 = 0.86f
    val alpha90 = 0.90f
    val alpha100 = 1.0f

    // ── Standard Card Shapes ───────────────────────────────────────
    val cardCorner = RoundedCornerShape(radius4xl)
    val cardCornerRounded = RoundedCornerShape(radius5xl)
    val cardCornerSm = RoundedCornerShape(radiusXl)
    val cardCornerXs = RoundedCornerShape(radiusLg)
    val cardCornerCircle = RoundedCornerShape(radiusFull)

    // ── Standard Card Specs ────────────────────────────────────────
    object Card {
        val cornerRadius = radius4xl
        val cornerRadiusRounded = radius5xl
        val cornerRadiusSm = radiusXl
        val cornerRadiusXs = radiusLg
        val padding = space16
        val paddingSm = space14
        val paddingLg = space18
        val elevationDefault = elevationXs
        val elevationRaised = elevationMd
        val elevationHighlight = elevationLg
        val strokeDefault = strokeThin
        val strokeEmphasized = strokeMedium
        val strokeProminent = strokeThick
        val iconBackgroundRadius = radiusMd
        val iconBackgroundRadiusSm = radiusSm
        val iconBackgroundRadiusLg = radiusLg
        val iconSize = 42.dp
        val iconSizeSm = 36.dp
        val iconSizeLg = 54.dp
        val iconInnerSize = 22.dp
        val iconInnerSizeSm = 18.dp
        val iconInnerSizeLg = 28.dp
    }

    // ── Standard Section Specs ─────────────────────────────────────
    object Section {
        val verticalSpacing = space16
        val verticalSpacingSm = space12
        val verticalSpacingLg = space20
        val horizontalPadding = space20
        val horizontalPaddingCompact = space16
        val topPadding = space16
        val bottomPadding = space32
        val headerBottomPadding = space4
        val labelTopPadding = space8
    }

    // ── Standard Screen Specs ──────────────────────────────────────
    object Screen {
        val horizontalPadding = space20
        val horizontalPaddingCompact = space16
        val topPadding = space16
        val bottomPadding = space32
        val fabBottomPadding = space20
    }

    // ── Standard Button Specs ──────────────────────────────────────
    object Button {
        val cornerRadius = radiusXl
        val cornerRadiusLg = radius5xl
        val height = 52.dp
        val heightSm = 48.dp
        val heightLg = 56.dp
        val paddingHorizontal = space18
        val iconSize = 24.dp
    }

    // ── Standard Input Specs ───────────────────────────────────────
    object Input {
        val cornerRadius = radiusLg
        val padding = space18
    }

    // ── Standard List Specs ────────────────────────────────────────
    object List {
        val itemSpacing = space12
        val itemSpacingSm = space8
        val itemSpacingLg = space16
    }

    // ── Standard Icon Specs ────────────────────────────────────────
    object Icon {
        val sizeXs = 16.dp
        val sizeSm = 18.dp
        val sizeMd = 20.dp
        val sizeLg = 22.dp
        val sizeXl = 24.dp
        val size2xl = 28.dp
        val size3xl = 32.dp
        val touchTarget = 48.dp
    }

    // ── Standard Badge Specs ───────────────────────────────────────
    object Badge {
        val cornerRadius = radiusFull
        val paddingHorizontal = space10
        val paddingVertical = space4
        val paddingHorizontalSm = space6
        val paddingVerticalSm = space2
    }

    // ── Standard Divider Specs ─────────────────────────────────────
    object Divider {
        val strokeWidth = strokeThin
        val paddingVertical = space4
    }
}
