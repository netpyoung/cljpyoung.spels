(ns cljpyoung.spels
  (:gen-class))

;; 상수.
(def 지도
  {:거실
   {:설명
    "[마법사의 집 - `거실`]: `마법사`가 소파에 코를 골며 자고있다."
    :경로 {:서쪽 [:문 :정원]
           :위층 [:계단 :다락방]}}

   :정원
   {:설명
    "[아름다운 `정원`]: `우물`이 앞에 보인다"
    :경로 {:동쪽 [:문 :거실]}}

   :다락방
   {:설명
    "[마법사의 집 - `다락방`]: 구석에 `용접`을 할 수 있는 화로가 있다"
    :경로 {:아래층 [:계단 :거실]}}})
(def 모든-물체들 [:위스키병 :양동이 :개구리 :사슬])

;; 변수.
(def &물체-위치
  (atom
   {:위스키병 :거실
    :양동이 :거실
    :사슬 :정원
    :개구리 :정원}))
(def &현재장소 (atom :거실))
(def &사슬을-용접하였는가 (atom false))
(def &양동이를-채웠는가 (atom false))

;; private functions
(defn- describe-location [game-map location]
  (-> game-map (get location) :설명))

(defn- describe-path [path]
  (let [[direction [via _]] path]
    (format "`%s`으로 가는 %s이 있다" (name direction) (name via))))

(defn- describe-paths [game-map location]
  (->> (get game-map location)
       :경로
       (mapv describe-path)))

(defn- at? [obj-loc obj loc]
  (= (get obj-loc obj) loc))

(defn- describe-floor [obj-loc objs loc]
  (->> objs
       (filter #(at? obj-loc % loc))
       (mapv #(format "`%s`(이/가) 바닦에 있다" (name %)))))

(defn- look
  ([] (look 모든-물체들 지도 @&물체-위치 @&현재장소))
  ([objects game-map object-location location]
   (-> [(describe-location game-map location)
        (describe-paths game-map location)
        (describe-floor object-location objects location)]
       (flatten)
       (vec))))

(defn- walk-direction [loc]
  (let [next (->> @&현재장소 지도 :경로 loc)]
    (if next
      (let [[_ to] next]
        (do
          (reset! &현재장소 to)
          (look)))
      ["그쪽으로 갈 수 없습니다"])))

(defn- pickup-object [object]
  (if-not (at? @&물체-위치 object @&현재장소)
    (format "여기에는 `%s`(이/가) 없습니다" (name object))
    (do
      (swap! &물체-위치 assoc object :주인공)
      (format "`%s`(을/를) 집어들었습니다" (name object)))))

(defn- inventory []
  (filter #(at? @&물체-위치 % :주인공) 모든-물체들))

(defn- have? [object]
  (->> (inventory)
       (some #{object})
       (some?)))

(defmacro defspel [& rest] `(defmacro ~@rest))

(defspel def-action [command subj obj place & args]
  `(defspel ~command [subject# object#]
     (let [subject# (keyword (str subject#))
           object# (keyword (str object#))]
       `(if (and (= @&현재장소 ~'~place)
                 (= ~subject# ~'~subj)
                 (= ~object# ~'~obj)
                 (have? ~'~subj))
          ~@'~args
          ~(str "그렇게 " '~command "할 수는 없습니다")))))

;; 명령어.
(def 둘러보기 look)
(def 소지품확인 inventory)

(defspel 이동 [direction]
  (let [dir (keyword (str direction))]
    `(walk-direction ~dir)))

(defspel 집어들기 [& objects]
  (let [objects (mapv #(keyword (str %)) objects)]
    `(vec (for [object# ~objects]
            (pickup-object object#)))))

(def-action 용접
  :사슬 :양동이 :다락방
  (if-not (have? :양동이)
    ["`양동이`를 가지고 있지 않습니다."]
    (do (reset! &사슬을-용접하였는가 true)
        ["`사슬`이 `양동이`에 단단히 용접되었습니다."])))

(def-action 던지기
  :양동이 :우물 :정원
  (if-not @&사슬을-용접하였는가
    ["물에 닿지 않습니다."]
    (do (reset! &양동이를-채웠는가 true)
        ["`양동이`에 물을 가득 채웠습니다."])))

(def-action 끼얹기
  :양동이 :마법사 :거실
  (cond (not @&양동이를-채웠는가)
        ["`양동이`가 비어있습니다"]

        (have? :개구리)
        ["`마법사`는 당신이 `개구리`를 훔친 것을 알아챘습니다."
         "그는 매우 화가나서, `당신`을 지옥으로 보내버렸습니다."
         "실패! 게임 끝."]

        :else
        ["`마법사`는 잠에서 깨고, `당신`을 따뜻히 맞아주었습니다."
         "그는 저-탄수화물 도넛을 `당신`에게 건냈습니다."
         "성공! 게임 끝."]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;; (둘러보기)
;; (집어들기 위스키병 양동이)
;; (이동 서쪽)
;; (집어들기 사슬)
;; (이동 동쪽)
;; (이동 위층)
;; (용접 사슬 양동이)
;; (이동 아래층)
;; (이동 서쪽)
;; (던지기 양동이 우물)
;; (이동 동쪽)
;; (끼얹기 양동이 마법사)
