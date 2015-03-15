(ns middlebrow-thrust.core
  (:require [middlebrow.core :refer :all]
            [middlebrow.util :refer :all]
            [clj-thrust.core :as thrust]
            [clj-thrust.window :as w]))

(defprotocol IThrustBrowser
  (listen-unresponsive [self handler])
  (listen-responsive [self handler])
  (listen-worker-crashed [self handler])
  (listen-remote [self handler])
  (set-kiosk [self kiosk])
  (open-devtools [self])
  (close-devtools [self])
  (kiosked? [window])
  (devtools-opened? [window]))

(defrecord ThrustBrowser [process window]
  IBrowser
  (show [self]
    (w/show window))

  (hide [self]
    ; TODO: Not quite the same as "hide". Once Thrust supports this operation
    ; update this line of code.
    (w/focus window false))

  (activate [self]
    (w/focus window true))

  (deactivate [self]
    (w/focus window false))

  (close [self]
    (w/close window))

  (visible? [self]
    ; TODO: Since there is currently no way to truly hide a Thrust window, it can
    ; only be not visible if the window was closed.
    (not (w/closed? window)))

  (minimize [self]
    (w/minimize window))

  (maximize [self]
    (w/maximize window))

  (minimized? [self]
    (w/minimized? window))

  (maximized? [self]
    (w/maximized? window))

  (set-fullscreen [self fullscreen]
    (w/set-fullscreen window fullscreen))

  (fullscreen? [self]
    (w/fullscreen? window))

  (get-title [self]
    (throw (UnsupportedOperationException. "Thrust does not support getting the title from the server side. Obtain it from the client side instead.")))

  (set-title [self title]
    (w/set-title window title))

  (get-x [self]
    (:x (w/position window)))

  (set-x [self x]
    (w/move window x (get-y self)))

  (get-y [self]
    (:y (w/position window)))

  (set-y [self y]
    (w/move window (get-x self) y))

  (get-position [self]
    (let [{:keys [x y]} (w/position window)]
      [x y]))

  (set-position [self position]
    (let [{:keys [x y]} position]
      (w/move window x y)))

  (set-position [self x y]
    (w/move window x y))

  (get-width [self]
    (:width (w/size window)))

  (set-width [self width]
    (w/resize window width (get-height self)))

  (get-height [self]
    (:height (w/size window)))

  (set-height [self height]
    (w/resize window (get-width self) height))

  (get-size [self]
    (let [{:keys [width height]} (w/size window)]
      [width height]))

  (set-size [self size]
    (let [[width height] size]
      (w/resize window width height)))

  (set-size [self width height]
    (w/resize window width height))

  (get-url [self]
    (throw (UnsupportedOperationException. "Thrust does not support getting the URL from the server side. Obtain it from the client side instead.")))

  (set-url [self url]
    (throw (UnsupportedOperationException. "Thrust does not support setting the URL from the server side (except upon window creation). Set the URL from the client side instead.")))

  (listen-closed [self handler]
    (w/listen-closed window
      (fn [e]
        (handler {:event e}))))

  (listen-focus-gained [self handler]
    (w/listen-focus window
      (fn [e]
        (handler {:event e}))))

  (listen-focus-lost [self handler]
    (w/listen-blur window
      (fn [e]
        (handler {:event e}))))

  (container-type [self] :thrust)

  (start-event-loop [self])
  (start-event-loop [self error-fn])

  IThrustBrowser
  (listen-unresponsive [self handler]
    (w/listen-unresponsive window  #(handler {:event %})))

  (listen-responsive [self handler]
    (w/listen-responsive window  #(handler {:event %})))

  (listen-worker-crashed [self handler]
    (w/listen-worker-crashed window  #(handler {:event %})))

  (listen-remote [self handler]
    (w/listen-remote window  #(handler {:event %})))

  (set-kiosk [self kiosk]
    (w/set-kiosk window kiosk))

  (open-devtools [self]
    (w/open-devtools window))

  (close-devtools [self]
    (w/close-devtools window))

  (kiosked? [self]
    (w/kiosked? window))

  (devtools-opened? [self]
    (w/devtools-opened? window)))

(defn style->has-frame [style]
  (case style
    :undecorated false
    true))

(defn create-window [& {:keys [url x y width height title style process thrust-directory]
                        :as   opts}]
  (let [process (or process (thrust/create-process thrust-directory))
        window (w/create-window process
                 :root-url (or url "about:blank")
                 :title (or title "Middlebrow")
                 :size {:width  (or width 400)
                        :height (or height 300)}
                 :has-frame (style->has-frame style))]
    (when (or x y)
      (w/move window (or x 0) (or y 0)))

    (->ThrustBrowser process window)))

(defn destroy-process [process]
  (thrust/destroy-process process))

(defn destroy-process-of [window]
  (thrust/destroy-process (:process window)))
