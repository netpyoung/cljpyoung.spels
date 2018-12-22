 [lisperati의 spels](http://www.lisperati.com/casting.html)에 나온 예제를 다시 작성해보았습니다.

 [clojure의 예제](http://www.lisperati.com/clojure-spels/casting.html)도 있으나, `quote`를 이용한 심볼의 중요성을 부각시키고자 하는 의도는 알겠으나, `common lisp`와 달리 `clojure`에서 저렇게 코드를 짜버리면 가독성 자체가 나빠져서, 오히려 배우려 하는 의지가 꺽이지 않을까 하는 생각이 듭니다.


* https://github.com/boot-clj
* https://github.com/boot-clj/boot-new
* https://lispkorea.github.io/clojure/setup_windows

그럼 여행을 떠나볼까요?

```
boot -d boot/new new -t app -n cljpyoung.spels
cd cljpyoung.spels
boot repl
```
