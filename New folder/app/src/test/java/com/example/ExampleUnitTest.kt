package com.example

import com.example.data.AiModelOption
import com.example.data.FloatingTaskDisplayMode
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testAiModelOptionValues() {
    val pro = AiModelOption.GEMINI_PRO_3_1
    val flash = AiModelOption.GEMINI_FLASH_LITE_3_8

    assertEquals("gemini-pro-3.1", pro.modelId)
    assertEquals("gemini-flash-lite-3.8", flash.modelId)
    assertTrue(pro.displayName.contains("3.1 Pro"))
    assertTrue(flash.displayName.contains("3.8 Flash Lite"))
  }

  @Test
  fun testFloatingTaskDisplayMode() {
    val inline = FloatingTaskDisplayMode.INLINE
    val bottom = FloatingTaskDisplayMode.SEPARATED_BOTTOM

    assertNotEquals(inline, bottom)
    assertTrue(inline.label.contains("مدمجة"))
    assertTrue(bottom.label.contains("مفصولة"))
  }
}
