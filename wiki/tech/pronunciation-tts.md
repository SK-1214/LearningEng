---
title: 발음 · TTS
category: tech
status: draft
sources: [raw/요구사항설계초안.txt, raw/wordlists/spelling bee contest.xlsx]
updated: 2026-09-26
---

# 발음 · TTS

## 원본 초안 (출처: raw/요구사항설계초안.txt)
- 브라우저 Web Speech API(`speechSynthesis`) 추천: 무료, 음원·API 키 불필요, 다만 발음이 다소 기계적
- 나중에 단어별 녹음 mp3로 확장 가능성

## 안드로이드로 바꿀 때
- 대응 기술: 안드로이드 내장 `android.speech.tts.TextToSpeech` (영어 `Locale.US`). 오프라인에서도 동작하고 무료.
- 기기에 설치된 TTS 엔진(대부분 Google TTS)에 따라 품질이 달라진다. 영어 음성 데이터가 없으면 설치 안내가 필요하다.
- 제안: 말하기 속도(`setSpeechRate`)를 0.8 정도로 낮춰 아이가 잘 들을 수 있게 한다.
- 확장: 엑셀에 오디오 파일명 컬럼을 두어 녹음 파일을 우선 쓰고, 없으면 TTS로 대체하는 방식도 가능.

## 실제 단어에서 확인할 점 (출처: raw/wordlists/spelling bee contest.xlsx)
- 동형이의어·다음어: **`read`**(현재형 /riːd/ vs 과거형 /rɛd/, 여기서는 "읽다"이므로 /riːd/), **`wind`**(바람 /wɪnd/ vs 감다 /waɪnd/), **`right`**/`write`, `too`/`two`처럼 소리가 같은 단어. TTS가 단어만 읽으면 발음이 틀리거나 철자를 헷갈릴 수 있다.
  - 제안: 스펠링 비 대회처럼 **예문과 함께 읽어 주는 기능**(엑셀에 `sentence` 열을 선택 항목으로 추가) 또는 단어별 발음 덮어쓰기
- `take off`, `movie star` 같은 구는 TTS로 자연스럽게 읽힌다.
- 레슨 3 추가(2026-09-27): **`wear`(입다) ↔ `where`**, `right` ↔ `write`처럼 소리가 같은 단어가 있다. 뜻을 숨기는 대회 연습에서는 "💡 뜻 보기"로 구분한다.
- 제안: 단어 전체를 TTS로 재생해 보고 이상한 발음을 기록하는 점검을 한다.

## 미결정
- TTS 품질로 충분한가? → [Q-3](../open-questions.md#q-3)
