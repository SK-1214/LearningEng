---
title: "ADR 0004: 레벨 3(쓰기)은 안드로이드 기본 키보드 사용"
category: decision
status: decided
sources: []
updated: 2026-09-26
---

# ADR 0004: 레벨 3(쓰기)은 안드로이드 기본 키보드 사용

- **결정일**: 2026-09-26 (사용자 결정)
- **해결한 질문**: [Q-11](../open-questions.md#q-11)
- **관련**: [ADR 0003 키보드 직접 입력](0003-keyboard-spelling-input.md)

## 결정
레벨 3(쓰기)에서는 앱 전용 키보드를 만들지 않고 **기기의 기본 키보드(Gboard, 삼성 키보드 등)** 로 입력한다.

## 이유
- 실제 기기 사용감과 같아서 대회 환경에 가깝다.
- 전용 키보드를 만들지 않아도 되므로 구현량이 줄어든다.

## 구현 시 반드시 지킬 것
기본 키보드는 자동완성·자동수정 때문에 **정답을 대신 보여줄 수 있다.** 다음을 적용하고 실제 기기에서 확인한다.
- `TextField`의 `KeyboardOptions`:
  - 자동수정 끔 (`autoCorrectEnabled = false`)
  - 자동 대문자 끔 (`capitalization = KeyboardCapitalization.None`)
  - 추천 단어 줄을 없애기 위해 `keyboardType = KeyboardType.Password`를 쓰고 `visualTransformation = VisualTransformation.None`으로 글자는 보이게 한다. 많은 키보드가 비밀번호 입력란에서는 추천 단어를 띄우지 않는다.
  - `imeAction = ImeAction.Done` → 키보드의 완료 키로 채점
- **한/영 전환 문제**: 아이 기기의 키보드가 한글 상태일 수 있다. 제안: 영어 입력 힌트(`hintLocales`, 지원하는 Compose 버전인지 확인 필요)를 주고, 한글이 입력되면 "영어 키보드로 바꿔요" 안내를 띄운다.
- 입력 필터: 영문자와 공백만 받는다(`take off`, `movie star`).
- 레이아웃: 키보드가 화면 아래 절반을 가리므로 `imePadding()`을 쓰고, 다시 듣기 버튼과 진행률은 키보드 위에 보이게 한다.
- **실기기 테스트 항목**: 추천 단어 줄이 뜨지 않는지, 맞춤법 밑줄이 없는지를 Gboard와 삼성 키보드에서 각각 확인한다.

## 실기기 확인 (2026-09-26, 갤럭시 탭 S7+ / 삼성 키보드)
- ✅ `KeyboardType.Password` + `VisualTransformation.None`: 삼성 키보드가 **영문 자판으로 열리고**, 글자를 입력해도 **추천 단어 줄이 나타나지 않았다**(도구 아이콘 줄만 보임). 입력한 글자는 보인다.
- ⚠️ 가로 모드에서 키보드가 화면 아래 절반을 가려 "확인" 버튼이 반쯤 가려졌다 → 버튼을 입력칸 오른쪽으로 옮겼다.
- 관찰: 실제 사용자가 `take off`를 `takeoff`로 입력했다 → 띄어쓰기가 있는 단어에 "(2단어 · 띄어쓰기)" 힌트를 추가했다.
- Gboard는 아직 확인하지 않았다(이 기기에서는 쓰지 않음).

## 영향
- [학습 레벨](../learning/learning-levels.md)의 "앱 전용 큰 알파벳 키보드" 제안은 폐기한다.
