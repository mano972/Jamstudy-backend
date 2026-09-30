// Adds the dark/light switch as the last item in the header nav (far right,
// next to the language flags), instead of a floating button - the navbar is
// injected by unistart.js's getPageHeader(), and this page also has its own
// fixed bottom-left CTAs (feedback, review search) that a floating toggle
// would collide with on small screens. The early FOUC-prevention snippet in
// each page's <head> already applies a stored explicit preference before CSS
// paints - this file only builds the button, wires clicks, and keeps the
// icon in sync.
(function () {

    function currentTheme() {
        return document.documentElement.getAttribute("data-theme") === "dark" ? "dark" : "light";
    }

    function applyIcon(btn) {
        var isDark = currentTheme() === "dark";
        // The site's FontAwesome kit only ships the solid ("fas") weight for
        // free-tier icons - the old v4 outline names (fa-sun-o/fa-moon-o)
        // resolve to the regular weight, which isn't loaded, and render blank.
        btn.innerHTML = isDark
            ? '<i class="fas fa-sun" aria-hidden="true"></i>'
            : '<i class="fas fa-moon" aria-hidden="true"></i>';
        var label = isDark ? "Comută la mod luminos" : "Comută la mod întunecat";
        btn.setAttribute("aria-label", label);
        btn.title = label;
    }

    function buildButton() {
        var li = document.createElement("li");
        var btn = document.createElement("a");
        btn.id = "theme-toggle-btn";
        btn.href = "#";
        btn.className = "btn btn-simple";
        btn.setAttribute("role", "button");

        applyIcon(btn);

        btn.addEventListener("click", function (e) {
            e.preventDefault();
            var next = currentTheme() === "dark" ? "light" : "dark";
            document.documentElement.setAttribute("data-theme", next);
            try {
                localStorage.setItem("theme", next);
            } catch (err) { /* storage unavailable (private mode, etc.) */ }
            applyIcon(btn);
        });

        li.appendChild(btn);
        return li;
    }

    function init() {
        var navbar = document.getElementById("demo-navbar");
        if (!navbar) {
            return;
        }

        var insert = function () {
            var list = navbar.querySelector(".navbar-nav");
            if (!list || document.getElementById("theme-toggle-btn")) {
                return false;
            }
            list.appendChild(buildButton());
            return true;
        };

        // getPageHeader() (unistart.js) fills #demo-navbar via innerHTML, on a
        // jQuery-ready callback registered earlier in the page than this
        // script - it should already be there, but a MutationObserver makes
        // this robust either way instead of depending on script order.
        if (insert()) {
            return;
        }
        var observer = new MutationObserver(function () {
            if (insert()) {
                observer.disconnect();
            }
        });
        observer.observe(navbar, { childList: true, subtree: true });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }

})();
