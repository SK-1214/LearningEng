---
title: "ADR 0001: 네이티브 안드로이드 앱으로 개발"
category: decision
status: decided
sources: [raw/요구사항설계초안.txt]
updated: 2026-09-26
---

# ADR 0001: 네이티브 안드로이드 앱으로 개발

- **결정일**: 2026-09-26 (사용자 결정)
- **해결한 질문**: [Q-1](../open-questions.md#q-1)

## 배경
원본 초안(`raw/요구사항설계초안.txt`)은 단일 웹 페이지 앱이었지만, 목표는 안드로이드 앱이다. 선택지는 네이티브로 새로 만드는 것과 웹앱을 WebView/PWA로 감싸는 것이었다.

## 결정
**Kotlin + Jetpack Compose 네이티브 앱**으로 만든다.

## 이유
- 안드로이드 `TextToSpeech`를 직접 쓸 수 있어 오프라인에서도 발음 재생이 안정적이다.
- 초1용 큰 터치 UI, 카드 배열 애니메이션, 앱 전용 키보드를 Compose로 구현하기 쉽다.
- Room 등으로 학습 기록을 기기에 안정적으로 저장할 수 있다. 브라우저 저장소처럼 초기화될 위험이 없다.

## 영향
- 초안의 웹 기술(SheetJS, Web Speech API, 브라우저 저장소)은 안드로이드 대응 기술로 바꾼다. → [안드로이드 기술 스택](../tech/android-stack.md)
- 엑셀 파싱 라이브러리, Room/DataStore 채택은 이 ADR 범위 밖이며 아직 제안 단계다.
