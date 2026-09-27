---
title: 안드로이드 기술 스택
category: tech
status: draft
sources: [raw/wordlists/spelling bee contest.xlsx]
updated: 2026-09-26
---

# 안드로이드 기술 스택

**확정**: 네이티브 안드로이드, **Kotlin + Jetpack Compose** → [ADR 0001](../decisions/0001-native-android.md)

원본 초안의 웹 기술에 대응하는 안드로이드 기술이다. 언어/UI 외의 항목은 아직 **제안**이며, 확정되면 ADR로 남긴다.

| 영역 | 웹 초안 | 안드로이드 | 상태 |
|---|---|---|---|
| 언어/UI | HTML/CSS/JS | Kotlin + Jetpack Compose | ✅ 확정 |
| 엑셀 파싱 | SheetJS | 아래 참고 | 제안 |
| 발음 | Web Speech API | `TextToSpeech` → [발음·TTS](pronunciation-tts.md) | 제안 |
| 저장 | 브라우저 저장소 | Room + DataStore → [데이터 저장](storage.md) | 제안 |
| 파일 선택 | `<input type=file>` | Storage Access Framework (`OpenDocument`) | 제안 |
| 키보드 입력 | 텍스트 입력창 | 기본 키보드 + `TextField`(자동수정·추천 끔) → [ADR 0004](../decisions/0004-system-keyboard.md) | ✅ 확정 |

## 엑셀 파싱
실제 파일(`raw/wordlists/spelling bee contest.xlsx`)은 **시트 1개, 문자열은 sharedStrings, 병합 셀과 수식 없음**으로 매우 단순하다.
- **직접 파싱 (제안)**: xlsx는 zip 안에 XML이 들어 있는 형식이다. `ZipInputStream`으로 `xl/sharedStrings.xml`과 `xl/worksheets/sheet1.xml`을 꺼내고, 안드로이드 내장 `XmlPullParser`로 읽으면 외부 라이브러리 없이 수십 줄로 구현할 수 있다. 위키 ingest 때도 이 방식(zip 풀기 → XML 읽기)으로 이 파일을 정상적으로 읽었다.
  - 처리할 셀 타입: `t="s"`(공유 문자열), `t="inlineStr"`, 숫자. `4-1`이 날짜로 저장된 경우도 대비한다.
- **Apache POI**: 기능이 가장 많지만 무겁다(APK 용량 증가, 메서드 수 많음). 이 용도에는 과하다.
- **FastExcel reader** 등 가벼운 라이브러리: 직접 파싱이 복잡해지면 대안으로 쓴다.
