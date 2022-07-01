(ns clojurians-log.components.common
  (:require [clojurians-log.message.format :as mformat]
            [clojurians-log.components.icons :as icons]))

(defn sidebar []
  [:div {:class "bg-indigo-900 text-purple-300 flex-none p-4"
         :id "sidebar"}
   [:div {:class "cursor-pointer mb-4"}
    [:div
     {:class
      "bg-white h-12 w-12 flex items-center justify-center text-black text-2xl font-semibold rounded-lg mb-1 overflow-hidden"}
     [:img {:alt "", :src "https://twitter.com/tailwindcss/profile_image"}]]
    [:div {:class "text-center text-white opacity-50 text-sm"} "⌘1"]]
   [:div {:class "cursor-pointer mb-4"}
    [:div
     {:class
      "bg-indigo-300 opacity-25 h-12 w-12 flex items-center justify-center text-black text-2xl font-semibold rounded-lg mb-1 overflow-hidden"}
     "L"]
    [:div {:class "text-center text-white opacity-50 text-sm"} "⌘2"]]
   [:div {:class "cursor-pointer"}
    [:div
     {:class
      "bg-white opacity-25 h-12 w-12 flex items-center justify-center text-black text-2xl font-semibold rounded-lg mb-1 overflow-hidden"}
     [:svg
      {:class "fill-current h-10 w-10 block",
       :viewbox "0 0 20 20",
       :xmlns "http://www.w3.org/2000/svg"}
      [:path
       {:d
        "M16 10c0 .553-.048 1-.601 1H11v4.399c0 .552-.447.601-1 .601-.553 0-1-.049-1-.601V11H4.601C4.049 11 4 10.553 4 10c0-.553.049-1 .601-1H9V4.601C9 4.048 9.447 4 10 4c.553 0 1 .048 1 .601V9h4.399c.553 0 .601.447.601 1z"}]]]]]
  )

(defn channel-list [channels]
  [:div
   {:class
    "bg-indigo-700 text-purple-300 flex-none w-64 pb-6 overflow-hidden overflow-y-scroll
     absolute inset-y-0 left-0 transform -translate-x-full transition duration-200 ease-in-out
     md:relative md:translate-x-0 z-10"
    :id "sidebar"}
   [:div {:class "text-white mb-2 mt-3 px-4 flex justify-between"}
    [:div {:class "flex-auto"}
     [:h1 {:class "font-semibold text-xl leading-tight mb-1 truncate"}
      [:a {:href "/" :class "text-white"} "Clojurians Log v2"]]
     [:div {:class "flex items-center mb-6"}
      [:svg
       {:class "h-2 w-2 fill-current text-green mr-2", :viewbox "0 0 20 20"}
       [:circle {:cx "10", :cy "10", :r "10"}]]
      [:span {:class "text-white opacity-50 text-sm"} "Clojure programming"]]]
    #_[:div
       [:svg
        {:class "h-6 w-6 fill-current text-white opacity-25",
         :viewbox "0 0 20 20"}
        [:path
         {:d
          "M14 8a4 4 0 1 0-8 0v7h8V8zM8.027 2.332A6.003 6.003 0 0 0 4 8v6l-3 2v1h18v-1l-3-2V8a6.003 6.003 0 0 0-4.027-5.668 2 2 0 1 0-3.945 0zM12 18a2 2 0 1 1-4 0h4z",
          :fill-rule "evenodd"}]]]]
   [:div {:class "md:hidden"}
    [:div {:class "p-4"}
     [:form {:action "/search"}
      [:input
       {:class "appearance-none border border-grey rounded-lg py-2 pl-2"
        :placeholder "Search"
        :name "q"
        :type "search"}]]
     [:div
      {:class "absolute inset-y-0 left-0 pl-3 flex items-center justify-center"}
      [:svg
       {:class "fill-current text-grey h-4 w-4",
        :viewbox "0 0 20 20",
        :xmlns "http://www.w3.org/2000/svg"}
       [:path
        {:d
         "M12.9 14.32a8 8 0 1 1 1.41-1.41l5.35 5.33-1.42 1.42-5.33-5.34zM8 14A6 6 0 1 0 8 2a6 6 0 0 0 0 12z"}]]]]]
   [:div {:class "mb-8"}
    [:div {:class "px-4 mb-2 text-white flex justify-between items-center"}
     [:div {:class "opacity-75"} "Channels"]]
    (for [channel channels]
      [:a {:href (str "/" (:name channel))
           :class "block hover:bg-indigo-500 py-1 px-4 text-white"}
       (str "# " (:name channel))])]
   [:div
    [:div {:class "px-4 mb-2 text-white flex justify-between items-center"}
     [:div {:class "opacity-75"} "Apps"]]]])

(defn top-bar [title subtitle]
  [:div {:class "border-b flex px-6 py-2 items-center flex-none"}
   [:div {:class "flex flex-row justify-between w-full items-center"}
    [:div {:class "overflow-hidden w-full"}
     [:h3 {:class "text-grey-900 mb-1 font-extrabold"} title]
     (let [text (mformat/message->text subtitle {})]
       [:div {:class "text-grey-dark text-sm truncate"
              :title text}
        text])]
    [:button {:class "md:hidden p-4 focus:outline-none focus:bg-gray-700"
              :id "mobile-menu-btn"}
     [:div {:class "w-6 h-6"}
      icons/menu]]]
   [:div {:class "ml-auto hidden md:block"}
    [:div {:class "relative"}
     [:form {:action "/search"}
      [:input
       {:class "appearance-none border border-grey rounded-lg pl-8 pr-4 py-2"
        :placeholder "Search"
        :name "q"
        :type "search"}]]
     [:div
      {:class "absolute inset-y-0 left-0 pl-3 flex items-center justify-center"}
      [:svg
       {:class "fill-current text-grey h-4 w-4",
        :viewbox "0 0 20 20",
        :xmlns "http://www.w3.org/2000/svg"}
       [:path
        {:d
         "M12.9 14.32a8 8 0 1 1 1.41-1.41l5.35 5.33-1.42 1.42-5.33-5.34zM8 14A6 6 0 1 0 8 2a6 6 0 0 0 0 12z"}]]]]]])

(defn message [{:member/keys [image-192 display-name]
                :message/keys [text created-at deleted-ts]
                :keys [reactions]} member-cache-id-name]
  [:div {:class "flex items-start mb-4 text-sm"}
   [:img
    {:class "w-10 h-10 rounded mr-3",
     :src (if deleted-ts "/assets/imgs/trash.png" image-192)}]
   [:div {:class "flex-1 overflow-hidden"}
    (when-not deleted-ts
      [:div [:span {:class "font-bold"} display-name]
       [:span {:class "text-grey text-xs"} (str " " created-at)]])
    [:p {:class "text-black leading-normal"}
     (mformat/message->hiccup text member-cache-id-name)]
    [:div.slack-message__reactions {:class "mt-2"}
     (for [reaction reactions]
       [:div.slack-message__reaction
        (mformat/text->emoji (:reaction/reaction reaction))
        " "
        (:count reaction)])]]])

(defn render-replies [replies member-cache-id-name]
  [:div {:class "ml-2 pl-3 border-l-2 border-gray-100"}
   (for [msg replies]
     [message msg member-cache-id-name])])

(defn message-with-tag []
  [:div {:class "flex items-start mb-4 text-sm"}
   [:img
    {:class "w-10 h-10 rounded mr-3",
     :src "https://twitter.com/davidhemphill/profile_image"}]
   [:div {:class "flex-1 overflow-hidden"}
    [:div [:span {:class "font-bold"} "David Hemphill"]
     [:span {:class "text-grey text-xs"} "12:46"]]
    [:p {:class "text-black leading-normal"}
     [:a
      {:class "inline-block bg-blue-100 text-blue no-underline",
       :href "#"} "@Clojurians log"]
     " the size of the generated CSS is creating a singularity in space/time, we must stop adding more utilities before it's too late!"]]])

(defn slack-layout [{:keys [channels title subtitle]
                     :or {channels []
                          title "Archives"
                          subtitle "🦄 Try out the search feature -->"}} & body]
  [:div {:class "font-sans antialiased h-screen flex"}
   (channel-list channels)
   [:div {:class "flex-1 flex flex-col bg-white overflow-hidden"}
    [top-bar title subtitle]
    [:div {:class "px-6 py-4 flex-1 overflow-y-scroll"}
     body]]])

(defn home-page [{:keys [channels]}]
  [slack-layout {:channels channels}
   [:h2 {:class "mb-4 text-xl font-bold"} "👋 Welcome clojurians!"]

   [:div {:class "prose lg:prose-lg"}
    [:p "This is Clojurians Log v2 which is an archive of the clojurians slack."]

    [:p "The wealth of knowledge being shared on the clojurians slack server is
    immense.  Capturing, conserving, and making this discourse complete, easily
    accessible, and searchable should greatly benefit the community as a
    whole."]

    [:ul
     [:li "Read about the Clojurists Together funding action plan: "
      [:a {:href "https://oxal.org/blog/clojurians-log-v2-funding/"}
       "oxal.org/blog/clojurians-log-v2-funding/"]]

     [:li "Find the source code, create issues, or contribute at "
      [:a {:href "https://github.com/oxalorg/clojurians-log-v2"}
       "github.com/oxalorg/clojurians-log-v2"]]]

    [:p "This project has received funding for 3 months by Clojurists Together.
    Thanks to the amazing Clojurists Together team and the awesome folks of the
    clojure community for their support 🥳 🌸"]

    [:h4 "Searching the entire archive"]

    [:p "Use the top right box to search over ~2 million messages from the logs!
    The search queries supports some special syntax like: "]

    [:ul
     [:li "Search for `clojure` for a simple search"]
     [:li "Search for `clojure spaghetti` for messages containing both clojure and spaghetti (PS: you won't get back any results 😉)"]
     [:li "Search for `plant OR soil` for messages containing either plant or soil"]
     [:li "Search for `macro -magic` for finding a macro which isn't magical"]
     [:li "Search for `\"macro magic\"` for finding the most magical macros"]]

    [:p {:class "text-sm"} "Made with 💜 by "
     [:a {:href "https://twitter.com/oxalorg"} "@oxalorg"]]]])

(defn search-page [{:keys [query messages]}]
  [slack-layout {:title (str "Search results for \"" query "\"")
                 :subtitle (str "in entire clojurians slack archive")}
   [:p "Found " (-> messages first :full-count) " results" ]
   (for [msg messages]
     [message msg {}])])

(defn channel-page [{:keys [channels channel message-counts-by-date]}]
  [slack-layout
   {:channels channels :title (:name channel) :subtitle (:topic channel)}
   [:ul {:class "list-styled"}
    (for [{:keys [created-at count]} message-counts-by-date]
      [:li
       [:a {:href (str "/" (:name channel) "/" created-at)
            :class "text-indigo-700 p-1"}
        (str created-at "  --- (" count " messages)")]])]])

(defn channel-date-page [{:keys [channels channel messages replies date member-cache-id-name]}]
  [slack-layout
   {:channels channels :title (:name channel) :subtitle (:topic channel)}
   (for [msg messages]
     [:div
      [message msg member-cache-id-name]
      [render-replies (get replies (:message/id msg)) member-cache-id-name]])])
