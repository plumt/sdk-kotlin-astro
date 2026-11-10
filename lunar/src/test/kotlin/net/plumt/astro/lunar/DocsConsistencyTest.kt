package net.plumt.astro.lunar

import net.plumt.astro.lunar.model.LunarPhaseType
import net.plumt.astro.lunar.model.SolarTermType
import net.plumt.astro.lunar.model.SpecialMoonEventType
import net.plumt.astro.lunar.model.SpecialMoonType
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 코드의 정의에서 파생되는 공개 문서(README·ERRORS·CHANGELOG)의 값이 어긋나면 여기서 실패한다.
 */
class DocsConsistencyTest {

    private val root: File = generateSequence(File("").absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").isFile }
    private val readme = File(root, "README.md").readText()
    private val errors = File(root, "ERRORS.md").readText()
    private val changelog = File(root, "CHANGELOG.md").readText()
    private val version = File(root, "lunar/gradle.properties").readLines()
        .first { it.startsWith("VERSION_NAME=") }.substringAfter('=')

    @Test fun `README 설치 줄과 빌드 버전이 같다`() {
        val line = "net.plumt.astro:lunar:$version"
        assertTrue("README 에 설치 줄이 없다: $line", readme.contains(line))
    }

    @Test fun `README 는 API 레벨 표와 알려진 한계를 둔다`() {
        assertTrue(readme.contains("## API 레벨별 동작"))
        assertTrue(readme.contains("## 알려진 한계"))
        assertTrue(readme.contains("| 23~25 |"))
        assertTrue(readme.contains("| 26 이상 |"))
        assertTrue("README 가 desugaring 을 안내하지 않는다", readme.contains("coreLibraryDesugaring"))
    }

    @Test fun `CHANGELOG 는 현재 버전을 적는다`() {
        assertTrue("CHANGELOG 에 $version 이 없다", changelog.contains("### [$version]"))
    }

    @Test fun `지원 연도 범위가 문서와 같다`() {
        val min = LunarCalculator.MIN_YEAR
        val max = LunarCalculator.MAX_YEAR
        assertTrue("ERRORS.md 범위 불일치", errors.contains("MIN_YEAR`($min)..`MAX_YEAR`($max)"))
        assertTrue("README 범위 불일치", readme.contains("$min~$max"))
    }

    @Test fun `ERRORS 는 공개 예외 타입을 모두 적는다`() {
        val types = listOf(IllegalArgumentException::class.java, LunarCalculationException::class.java)
        val missing = types.map { it.simpleName }.filterNot { errors.contains("| `$it`") }
        assertTrue("ERRORS.md 예외 표에 없다: $missing", missing.isEmpty())
    }

    @Test fun `README 는 위상 이름을 모두 적는다`() {
        val missing = LunarPhaseType.values.map { it.koreanName.substringBefore('(') }.filterNot { readme.contains(it) }
        assertTrue("README 에 없는 위상: $missing", missing.isEmpty())
    }

    @Test fun `README 와 ERRORS 는 가짜 계산기를 안내한다`() {
        val name = FakeLunarCalculator::class.java.simpleName
        assertTrue(readme.contains(name))
        assertTrue(errors.contains(name))
    }

    @Test fun `README 옮기기 표는 sealed 값을 모두 적는다`() {
        val names = (LunarPhaseType.values + SpecialMoonType.values + SpecialMoonEventType.values).map { it.toString() }
        val table = readme.substringAfter("### 0.0.1 에서 옮기기").substringBefore("\n## ")
        val missing = names.filterNot { table.contains(it) }
        assertTrue("README 옮기기 표에 없는 값: $missing", missing.isEmpty())
    }

    @Test fun `24절기 수와 문서가 같다`() {
        assertTrue(SolarTermType.values.size == 24)
        assertTrue(readme.contains("${SolarTermType.values.size}절기"))
    }

    @Test fun `CHANGELOG 현재 버전은 표준 제목(추가·바뀜·깨짐·고침)만 쓴다`() {
        val section = changelog.substringAfter("### [$version]").substringBefore("\n### [")
        val headings = section.lines().filter { it.startsWith("#### ") }.map { it.removePrefix("#### ").trim() }
        val allowed = setOf("추가", "바뀜", "깨짐", "고침")
        assertTrue("허용되지 않은 제목: ${headings - allowed}", (headings - allowed).isEmpty())
        assertTrue("현재 버전 절에 제목이 없다", headings.isNotEmpty())
    }
}
