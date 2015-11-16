(ns hello-world.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as hiccup]
            [clojure.data.csv :as csv]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def db 
  (with-open [in-file (clojure.java.io/reader "resources/revit.csv")]
    (doall
     (csv/read-csv in-file))))

(def upcol 23) ;;x column of spreadsheet
(def downcol 15) ;;p column of spreadsheet
(def urlprefix "http://colinmbp.local:3000/")
;just the upstream

(defn upstream [key]
  (filter (fn [row]
            (= (str key) (nth row upcol)))
          db))

(defn downstream [key]
  (filter (fn [row]
            (= key (nth row downcol)))
          db))


(def header "<html lang='en'>
  <head>
    <meta charset='utf-8'>
    <meta http-equiv='X-UA-Compatible' content='IE=edge'>
    <meta name='viewport' content='width=device-width, initial-scale=1'>
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <title>AEC-HACKATHON</title>

    <!-- Bootstrap -->
    <link href='bs/css/bootstrap.min.css' rel='stylesheet'>

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src='https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js'></script>
      <script src='https://oss.maxcdn.com/respond/1.4.2/respond.min.js'></script>
    <![endif]-->
  </head>"
)



(def bodyinclude "
     <script type='text/javascript' src='js/qrcode.min.js'></script>          
    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src='https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js'></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src='bs/js/bootstrap.min.js'></script>
<div class='container' role='main'>"


)

(defn qrwrap [x] 
  (hiccup/html
   [:img [:g {:id "qrcode"}]]
          "<script type='text/javascript'>
  var qrcode = new QRCode(document.getElementById('qrcode'), {
      width : 100,
      height : 100,
      useSVG : true, 
      colorDark : '#000000',
    colorLight : '#ffffff',
  });
      qrcode.makeCode(\"" urlprefix x "\"); </script>"))

(defroutes app-routes
  (GET "/" [] (hiccup/html [:html header [:body bodyinclude [:h1 "Use a QR scanner, or add a panel name to the URL to search"]]]))



;;debug:
;;  (GET "/" request
;;   (str request))
  
  (GET "/:foo" {{foo :foo} :params}
       
       (hiccup/html
        [:html         
         header
         [:body

          bodyinclude
          [:span {:class "row"}
          [:h1 foo]
          (qrwrap foo)]

          [:h3 
           (str "Upstream components for " foo ":" )]
          [:span {:class "row"}
          (for [x (upstream foo)]
             [:button {:class "btn btn-med btn-info"}[:a {:href  (nth x downcol)} (nth x downcol) " "]])]
          [:h3
           (str "Downstream components for " foo ":" )]
          [:span {:class "row"}
          (for [x (downstream foo)]
            [:button {:class "btn btn-med btn-info"}[:a {:href  (nth x upcol)}  (nth x upcol) "  "]])]

          [:h1 
          (for [x (upstream foo)]
            [:button {:class "btn btn-med btn-warning"}[:a {:href  (nth x 4)} " PANEL SCHEDULE "]])]

          ]]))



  (route/resources "/")
  (route/not-found (hiccup/html [:html header [:body bodyinclude [:h2 "Couldn't find that panel, possibly a typo?"]]]))
)


(def app
  (wrap-defaults app-routes site-defaults))


