package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiExtensionPuzzle: ImageVector
    get() {
        if (_CiExtensionPuzzle != null) {
            return _CiExtensionPuzzle!!
        }
        _CiExtensionPuzzle = ImageVector.Builder(
            name = "CiExtensionPuzzle",
            defaultWidth = 30.dp,
            defaultHeight = 30.dp,
            viewportWidth = 30f,
            viewportHeight = 30f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(30f)
                    verticalLineToRelative(30f)
                    horizontalLineToRelative(-30f)
                    close()
                }
            ) {
                path(fill = SolidColor(Color(0xFF000000))) {
                    moveTo(4.515f, 25.493f)
                    curveTo(4.017f, 24.996f, 3.703f, 24.474f, 3.573f, 23.929f)
                    curveTo(3.438f, 23.378f, 3.45f, 22.849f, 3.609f, 22.342f)
                    curveTo(3.769f, 21.826f, 4.044f, 21.372f, 4.435f, 20.98f)
                    curveTo(4.681f, 20.734f, 4.942f, 20.546f, 5.217f, 20.415f)
                    curveTo(5.488f, 20.28f, 5.799f, 20.171f, 6.152f, 20.089f)
                    curveTo(6.485f, 20.007f, 6.688f, 19.872f, 6.76f, 19.684f)
                    curveTo(6.837f, 19.491f, 6.787f, 19.305f, 6.608f, 19.126f)
                    lineTo(3.928f, 16.446f)
                    curveTo(3.218f, 15.736f, 2.866f, 15.017f, 2.87f, 14.287f)
                    curveTo(2.875f, 13.549f, 3.24f, 12.817f, 3.964f, 12.092f)
                    lineTo(5.898f, 10.158f)
                    curveTo(6.072f, 9.985f, 6.256f, 9.936f, 6.449f, 10.014f)
                    curveTo(6.642f, 10.081f, 6.78f, 10.282f, 6.862f, 10.615f)
                    curveTo(6.944f, 10.977f, 7.05f, 11.296f, 7.18f, 11.571f)
                    curveTo(7.311f, 11.837f, 7.499f, 12.092f, 7.745f, 12.339f)
                    curveTo(8.136f, 12.73f, 8.59f, 13.005f, 9.107f, 13.165f)
                    curveTo(9.619f, 13.319f, 10.148f, 13.331f, 10.693f, 13.201f)
                    curveTo(11.244f, 13.066f, 11.766f, 12.752f, 12.258f, 12.259f)
                    curveTo(12.751f, 11.767f, 13.062f, 11.247f, 13.193f, 10.702f)
                    curveTo(13.328f, 10.151f, 13.316f, 9.622f, 13.156f, 9.115f)
                    curveTo(12.997f, 8.599f, 12.722f, 8.145f, 12.33f, 7.754f)
                    curveTo(12.084f, 7.507f, 11.828f, 7.319f, 11.563f, 7.189f)
                    curveTo(11.292f, 7.053f, 10.976f, 6.945f, 10.614f, 6.863f)
                    curveTo(10.281f, 6.781f, 10.078f, 6.645f, 10.005f, 6.457f)
                    curveTo(9.933f, 6.259f, 9.984f, 6.073f, 10.157f, 5.899f)
                    lineTo(12.092f, 3.965f)
                    curveTo(12.811f, 3.246f, 13.54f, 2.883f, 14.279f, 2.879f)
                    curveTo(15.013f, 2.869f, 15.735f, 3.219f, 16.445f, 3.929f)
                    lineTo(19.125f, 6.609f)
                    curveTo(19.304f, 6.788f, 19.487f, 6.841f, 19.676f, 6.768f)
                    curveTo(19.869f, 6.691f, 20.004f, 6.488f, 20.081f, 6.16f)
                    curveTo(20.168f, 5.803f, 20.279f, 5.489f, 20.414f, 5.218f)
                    curveTo(20.545f, 4.943f, 20.731f, 4.685f, 20.972f, 4.443f)
                    curveTo(21.368f, 4.047f, 21.822f, 3.772f, 22.334f, 3.617f)
                    curveTo(22.846f, 3.453f, 23.377f, 3.439f, 23.927f, 3.574f)
                    curveTo(24.473f, 3.704f, 24.995f, 4.018f, 25.492f, 4.516f)
                    curveTo(25.985f, 5.008f, 26.299f, 5.53f, 26.434f, 6.08f)
                    curveTo(26.564f, 6.626f, 26.55f, 7.157f, 26.39f, 7.674f)
                    curveTo(26.231f, 8.181f, 25.953f, 8.632f, 25.557f, 9.028f)
                    curveTo(25.316f, 9.27f, 25.06f, 9.458f, 24.789f, 9.593f)
                    curveTo(24.514f, 9.724f, 24.198f, 9.832f, 23.841f, 9.919f)
                    curveTo(23.512f, 9.997f, 23.309f, 10.132f, 23.232f, 10.325f)
                    curveTo(23.16f, 10.513f, 23.213f, 10.697f, 23.392f, 10.876f)
                    lineTo(26.072f, 13.556f)
                    curveTo(26.786f, 14.27f, 27.139f, 14.995f, 27.129f, 15.729f)
                    curveTo(27.12f, 16.463f, 26.755f, 17.19f, 26.035f, 17.909f)
                    lineTo(24.101f, 19.843f)
                    curveTo(23.927f, 20.017f, 23.744f, 20.07f, 23.551f, 20.003f)
                    curveTo(23.362f, 19.93f, 23.227f, 19.727f, 23.145f, 19.394f)
                    curveTo(23.063f, 19.032f, 22.955f, 18.716f, 22.819f, 18.445f)
                    curveTo(22.689f, 18.18f, 22.501f, 17.924f, 22.254f, 17.677f)
                    curveTo(21.863f, 17.286f, 21.409f, 17.011f, 20.892f, 16.852f)
                    curveTo(20.381f, 16.687f, 19.852f, 16.675f, 19.306f, 16.815f)
                    curveTo(18.76f, 16.946f, 18.241f, 17.257f, 17.749f, 17.75f)
                    curveTo(17.256f, 18.242f, 16.942f, 18.764f, 16.807f, 19.314f)
                    curveTo(16.672f, 19.855f, 16.684f, 20.384f, 16.843f, 20.901f)
                    curveTo(17.003f, 21.417f, 17.278f, 21.871f, 17.669f, 22.263f)
                    curveTo(17.915f, 22.509f, 18.171f, 22.697f, 18.437f, 22.827f)
                    curveTo(18.712f, 22.958f, 19.031f, 23.064f, 19.393f, 23.146f)
                    curveTo(19.726f, 23.228f, 19.927f, 23.366f, 19.994f, 23.559f)
                    curveTo(20.067f, 23.747f, 20.016f, 23.929f, 19.842f, 24.102f)
                    lineTo(17.908f, 26.037f)
                    curveTo(17.184f, 26.761f, 16.455f, 27.128f, 15.72f, 27.138f)
                    curveTo(14.991f, 27.142f, 14.269f, 26.787f, 13.555f, 26.073f)
                    lineTo(10.875f, 23.392f)
                    curveTo(10.696f, 23.214f, 10.51f, 23.163f, 10.317f, 23.24f)
                    curveTo(10.128f, 23.313f, 9.993f, 23.516f, 9.911f, 23.849f)
                    curveTo(9.829f, 24.201f, 9.723f, 24.515f, 9.592f, 24.791f)
                    curveTo(9.457f, 25.061f, 9.266f, 25.319f, 9.02f, 25.566f)
                    curveTo(8.629f, 25.957f, 8.178f, 26.234f, 7.666f, 26.399f)
                    curveTo(7.154f, 26.553f, 6.625f, 26.565f, 6.079f, 26.435f)
                    curveTo(5.529f, 26.3f, 5.007f, 25.986f, 4.515f, 25.493f)
                    close()
                }
            }
        }.build()

        return _CiExtensionPuzzle!!
    }

@Suppress("ObjectPropertyName")
private var _CiExtensionPuzzle: ImageVector? = null
