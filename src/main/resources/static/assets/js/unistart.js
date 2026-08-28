
$.ajaxSetup({
  headers: {
    Token: getUField("ut") ? "Bearer " + getUField("ut") : null,
    Code: window.location.href.includes("unistart.lt") ? "LT" : "RO"
  }
});

// Canonical faculty profile URL. Uses the SEO slug path (/facultate/{uni}/{fac})
// when the faculty carries slugs, else falls back to the legacy ?id= URL — which
// 301s to the slug path server-side. Accepts a faculty object or a bare id.
function facultyProfileUrl(faculty) {
	if (faculty && typeof faculty === "object") {
		if (faculty.universitySlug && faculty.facultySlug) {
			return "/facultate/" + faculty.universitySlug + "/" + faculty.facultySlug;
		}
		return "./profile.html?id=" + (faculty.facultyId || "");
	}
	return "./profile.html?id=" + (faculty || "");
}

// Escapes user/API-supplied text before it is concatenated into an HTML string
// and assigned via innerHTML/.html() elsewhere in this file and in the static pages.
function escapeHtml(value) {
	if (value === null || value === undefined) {
		return "";
	}
	return String(value)
		.replace(/&/g, "&amp;")
		.replace(/</g, "&lt;")
		.replace(/>/g, "&gt;")
		.replace(/"/g, "&quot;")
		.replace(/'/g, "&#39;");
}

function myAccount(el) {
	var jwtToken = getUField("ut");
	if (jwtToken) {
		location.href = "./user.html";
	} else {
		el.setAttribute("data-toggle", 'modal');
		el.setAttribute("data-target", '#login-modal');
	}
}

function myAccountText() {
	var jwtToken = getUField("ut");
	if (jwtToken) {
		document.getElementById('myaccount').innerHTML = '<i class="far fa-user" style="margin-right: 7px"></i><span data-i18n-key="header-footer-my-account">Contul meu</span>';
	} else {
		document.getElementById('myaccount').innerHTML = '<i class="far fa-user" style="margin-right: 7px"></i><span data-i18n-key="header-footer-login">Autentificare</span>';
	}
}

/* Facebook login is not currently offered on the site — SDK init disabled to avoid
   loading/talking to Facebook (and setting cookies) on every page load.
window.fbAsyncInit = function() {
    // FB JavaScript SDK configuration and setup
    FB.init({
      appId            : '369581668156591',
      autoLogAppEvents : true,
      xfbml            : true,
      version          : 'v11.0'
    });

    // Check whether the user already logged in
    FB.getLoginStatus(function(response) {
        if (response.status === 'connected') {
            //display user data
            // getFbUserData();
			var jwtToken = getUField("ut");
			if (!jwtToken) {
				 fbLogout();
			}

        }
    });
};


// Load the JavaScript SDK asynchronously
(function(d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) return;
    js = d.createElement(s); js.id = id;
    js.src = "//connect.facebook.net/en_US/sdk.js";
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));
*/

/* If browser back button was used, flush cache */
window.addEventListener( "pageshow", function ( event ) {
  var historyTraversal = event.persisted || 
                         ( typeof window.performance != "undefined" && 
                              window.performance.navigation.type === 2 );
  if ( historyTraversal ) {
    // Handle page restore.
    window.location.reload();
  }
});

function loadUser() {
	var jwtToken = getUField("ut");
	if (jwtToken) {
		var backendUrl = new URL(backendUrlRoot + "/v1/userprofile");
		
		$.ajax({
		  url: backendUrl,
		  type: "GET",
		  async: false,
		  success: function(response) {
			if (response.control.errorCode === 0) {
			
				var userResponse = response.result;
				var savedFaculties = userResponse.favoriteFaculties;
				var savedFacultiesIds = [];
				for (i in savedFaculties) {
					savedFacultiesIds.push(savedFaculties[i].facultyId);
				}
				var savedCompaniesIds =  response.result.favoriteCompanies; // list of ids
				var likedReviewsIds = userResponse.likedReviews;
				var likedQuestionsIds = userResponse.likedQuestions;
				var likedAnswersIds = userResponse.likedAnswers;
				var addedReviews = userResponse.addedReviews;

				setUField("usf", savedFacultiesIds);
				setUField("usc", savedCompaniesIds);
				setUField("ulr", likedReviewsIds);
				setUField("ulq", likedQuestionsIds);
				setUField("ula", likedAnswersIds);
				setUField("uar", addedReviews);
				

			} else {
				console.log(response.control.errorDescription);
			}

		  },
		  error: function(error) {
			checkLoggedInUser(error.status)
		  },
		  complete: function (data) {
		  }
		});
	}
}

/* Facebook login is not currently offered on the site (button is commented out) — disabled along with the SDK.
// Facebook login with JavaScript SDK
function fbLogin(e) {
	e.preventDefault();

    FB.login(function (response) {
        if (response.authResponse) {
            // Get and display the user profile data
            getFbUserData();
        } else {
			console.log("User cancelled login or did not fully authorize.");
        }
    }, {scope: 'email'});
}

// Fetch the user profile data from facebook
function getFbUserData(){
    FB.api('/me', {locale: 'en_US', fields: 'id,first_name,last_name,email'},
    function (response) {
		loginWithSocialMediaAccount(response, "FACEBOOK");
    });
}
*/

// The Google Sign-In SDK is not loaded on page load — clicking this button is the
// user's affirmative action that loads it (acts as consent for that specific purpose),
// consistent with the site's cookie consent approach for non-essential scripts.
function googleButtonClick(e) {
	e.preventDefault();

	if (window.google && window.google.accounts && window.google.accounts.id) {
		clickRenderedGoogleButton();
	} else {
		loadGoogleSignInScript(function() {
			// Give the library a brief moment to parse the DOM and render its buttons.
			setTimeout(clickRenderedGoogleButton, 200);
		});
	}
}

function loadGoogleSignInScript(onReady) {
	var existingScript = document.getElementById('google-gsi-script');
	if (existingScript) {
		existingScript.addEventListener('load', onReady, { once: true });
		return;
	}
	var script = document.createElement('script');
	script.id = 'google-gsi-script';
	script.src = 'https://accounts.google.com/gsi/client?hl=ro';
	script.async = true;
	script.onload = onReady;
	document.head.appendChild(script);
}

function clickRenderedGoogleButton() {
	if (document.getElementsByClassName("L5Fo6c-bF1uUb")[0]) {
		document.getElementsByClassName("L5Fo6c-bF1uUb")[0].click();
	} else if (document.getElementsByClassName("nsm7Bb-HzV7m-LgbsSe")[0]) {
		document.getElementsByClassName("nsm7Bb-HzV7m-LgbsSe")[0].click();
	}
}

function googleLogin(response) {
	let payload = parseGoogleJwt(response.credential);
	let userData = {};
	userData.email = payload.email;
	userData.first_name = payload.given_name;
	userData.last_name = payload.family_name;
	loginWithSocialMediaAccount(userData, "GOOGLE");
}

function parseGoogleJwt (token) {
	var base64Url = token.split('.')[1];
	var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
	var jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
		return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
	}).join(''));

	return JSON.parse(jsonPayload);
}

function loginWithSocialMediaAccount(userData, registerType) {
	
	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/loginSocialMedia");
	
	var email = userData.email;
	var firstName = userData.first_name;
	var lastName = userData.last_name;
	
	subscribeToNewsletter = false;
	if (document.getElementById("register-button")) {
		var newsletterCheckBoxElementId = $("label[for=newsletter-check]").attr("for");
		var newsletterCheckBoxElement = $("input[id='" + newsletterCheckBoxElementId + "']");
		if (newsletterCheckBoxElement.prop("checked") === true) {
			subscribeToNewsletter = true;
		}
	}
		
	var body = {
		email: email,
		firstName: firstName,
		lastName: lastName,
		registerType: registerType,
		subscribeToNewsletter: subscribeToNewsletter
	};
	
	if (document.getElementById("login-button")) {
		document.getElementById("login-button").disabled = true; 
		// document.getElementById("login-fb-button").disabled = true;
		document.getElementById("login-google-button").disabled = true;
	}
	if (document.getElementById("register-button")) {
		document.getElementById("register-button").disabled = true; 
		// document.getElementById("register-fb-button").disabled = true;
		document.getElementById("register-google-button").disabled = true;
	}
	
	$.ajax({
		url: backendUrl,
		type: 'POST',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		data: JSON.stringify(body),
		success: function (response) {
			localStorage.setItem("u", "{}");
			var jwtToken = response.result.jwtToken;
			var savedFaculties = response.result.favoriteFaculties;
			var savedFacultiesIds = [];
			for (i in savedFaculties) {
				savedFacultiesIds.push(savedFaculties[i].facultyId);
			}
			var savedCompaniesIds =  response.result.favoriteCompanies; // list of ids
			var likedReviewsIds = response.result.likedReviews;
			var likedQuestionsIds = response.result.likedQuestions;
			var likedAnswersIds = response.result.likedAnswers;
			var addedReviews = response.result.addedReviews;

			setUField("ut", jwtToken);
			setUField("usf", savedFacultiesIds);
			setUField("usc", savedCompaniesIds);
			setUField("ulr", likedReviewsIds);
			setUField("ulq", likedQuestionsIds);
			setUField("ula", likedAnswersIds);
			setUField("uar", addedReviews);

			redirectAfterLogin();
		},
		error: function(error) {
			if (error.status == 401) {
				if (document.getElementById('error-login')) {
					document.getElementById('error-login').innerHTML = "Datele de autentificare sunt incorecte.";
				}
				if (document.getElementById('error-register')) {
					document.getElementById('error-register').innerHTML = "Datele de autentificare sunt incorecte.";
				}
			} else {
				if (document.getElementById('error-login')) {
					document.getElementById('error-login').innerHTML = "A apărut o eroare. Te rugăm să încerci din nou mai târziu.";
				}
				if (document.getElementById('error-register')) {
					document.getElementById('error-register').innerHTML = "A apărut o eroare. Te rugăm să încerci din nou mai târziu.";
				}
			}
		
			if (document.getElementById("login-button")) {
				document.getElementById("login-button").disabled = false; 
				// document.getElementById("login-fb-button").disabled = false;
				document.getElementById("login-google-button").disabled = false;
			}
			if (document.getElementById("register-button")) {
				document.getElementById("register-button").disabled = false; 
				// document.getElementById("register-fb-button").disabled = false;
				document.getElementById("register-google-button").disabled = false;
			}
		}
	});
}

/* Facebook login is not currently offered on the site — logout helper disabled along with the SDK.
// Logout from facebook
function fbLogout() {
    FB.logout(function() {
    });
}
*/

function clearErrorLoginEmail() {
	document.getElementById('error-login-email').innerHTML = "";
}

function clearErrorLoginPass() {
	document.getElementById('error-login').innerHTML = "";
}

function clearErrorRegisterEmail() {
	document.getElementById('error-register-email').innerHTML = "";
}

function clearErrorRegisterPass() {
	document.getElementById('error-register').innerHTML = "";
}

function clearErrorRegisterConfirmPass() {
	document.getElementById('error-register').innerHTML = "";
}


function clearErrorResetPass() {
	document.getElementById('error-reset').innerHTML = "";
}

function clearErrorResetConfirmPass() {
	document.getElementById('error-reset').innerHTML = "";
}

function clearErrorForgotEmail() {
	document.getElementById('error-forgot-email').innerHTML = "";
}


// document.getElementById("login-button").onclick = function (e) {
function login(e) {
	e.preventDefault();
	
	var userEmail = document.getElementById("login-email").value.trim();
	var userPass = document.getElementById("login-pass").value.trim();
	
	var errorEmail = document.getElementById('error-login-email');
	var errorLogin = document.getElementById('error-login');
	
	var isInvalid = false;
	
	if (!userEmail) {
		errorEmail.innerHTML = "Câmp obligatoriu";
		isInvalid = true;
	} else if (!validateEmail(userEmail)) {
		errorEmail.innerHTML = "Adresa de email nu este validă";
		isInvalid = true;
	}
	if (!userPass) {
		errorLogin.innerHTML = "Câmp obligatoriu";
		isInvalid= true;
	}
	
	if (isInvalid) {
		return false;
	}
	
	document.getElementById("login-button").disabled = true; 
	// document.getElementById("login-fb-button").disabled = true;
	document.getElementById("login-google-button").disabled = true;

	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/login");
		
	var body = {
		email: userEmail,
		password: userPass
	};
	
	
	$.ajax({
		url: backendUrl,
		type: 'POST',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		data: JSON.stringify(body),
		success: function (response) {
			localStorage.setItem("u", "{}");
			var jwtToken = response.result.jwtToken;
			var savedFaculties = response.result.favoriteFaculties;
			var savedFacultiesIds = [];
			for (i in savedFaculties) {
				savedFacultiesIds.push(savedFaculties[i].facultyId);
			}
			var savedCompaniesIds =  response.result.favoriteCompanies; // list of ids
			var likedReviewsIds = response.result.likedReviews;
			var likedQuestionsIds = response.result.likedQuestions;
			var likedAnswersIds = response.result.likedAnswers;
			var addedReviews = response.result.addedReviews;

			setUField("ut", jwtToken);
			setUField("usf", savedFacultiesIds);
			setUField("usc", savedCompaniesIds);
			setUField("ulr", likedReviewsIds);
			setUField("ulq", likedQuestionsIds);
			setUField("ula", likedAnswersIds);
			setUField("uar", addedReviews);

			redirectAfterLogin();
		},
		error: function(error) {
			if (error.responseJSON) {
				if (error.responseJSON.control) {
					var errorDescription = error.responseJSON.control.errorDescription;
					if (error.responseJSON.control.errorCode === -83) { // email address was not yet confirmed
						var resendEmailString = '<a onclick="resendConfirmationEmail(&quot;'+userEmail+'&quot;, null)"><b>&nbspRetrimite email de confirmare.</b></a>';
						errorDescription = errorDescription + resendEmailString;
					}
					errorLogin.innerHTML = errorDescription;
				} else {
					errorLogin.innerHTML = "A apărut o eroare. Te rugăm să încerci din nou mai târziu.";
				}
			} else {
				errorLogin.innerHTML = "A apărut o eroare. Te rugăm să încerci din nou mai târziu.";
			}
			// if (error.status == 401) {
				// errorLogin.innerHTML = "Datele de autentificare sunt incorecte.";
			// } else {
				// errorLogin.innerHTML = "A apărut o eroare. Te rugăm să încerci din nou mai târziu.";
			// }
		
			document.getElementById("login-button").disabled = false;
			// document.getElementById("login-fb-button").disabled = false;
			document.getElementById("login-google-button").disabled = false;
		}
	});
	
}

// document.getElementById("register-button").onclick = function (e) {
function register(e) {
	e.preventDefault();
	
	var userEmail = document.getElementById("register-email").value.trim();
	var userPass = document.getElementById("register-pass").value.trim();
	var userConfPass = document.getElementById("register-confirm-pass").value.trim();
	
	var errorEmail = document.getElementById('error-register-email');
	var errorRegister = document.getElementById('error-register');
	
	var isInvalid = false;
	
	const passMin = 7;
	
	if (!userEmail) {
		errorEmail.innerHTML = "Câmp obligatoriu";
		isInvalid = true;
	} else if (!validateEmail(userEmail)) {
		errorEmail.innerHTML = "Adresa de email nu este validă";
		isInvalid = true;
	}
	
	var acceptTermsAndConditions = false;
	var policyCheckBoxElementId = $("label[for=policy-check]").attr("for");
	var policyCheckBoxElement = $("input[id='" + policyCheckBoxElementId + "']");
	if (policyCheckBoxElement.prop("checked") != true) {
		errorRegister.innerHTML = "Trebuie să accepți Termenii și condițiile de utilizare și Politica de prelucrare a datelor";
		isInvalid = true;
	} else {
		acceptTermsAndConditions = true;
	}
	
	if (!userPass) {
		errorRegister.innerHTML = "Câmp obligatoriu";
		isInvalid = true;
	} else if (userPass.length < passMin) {
		errorRegister.innerHTML = "Parola trebuie să fie de minim 7 caractere.";
		isInvalid = true;
	} else if (!validatePass(userPass)) {
		errorRegister.innerHTML = "Parola trebuie să conțină litere și cifre.";
		isInvalid = true;
	} else if (userPass != userConfPass) {
		errorRegister.innerHTML = "Cele două parole nu se potrivesc.";
		isInvalid = true;
	}
	
	var subscribeToNewsletter = false;
	var newsletterCheckBoxElementId = $("label[for=newsletter-check]").attr("for");
	var newsletterCheckBoxElement = $("input[id='" + newsletterCheckBoxElementId + "']");
	if (newsletterCheckBoxElement.prop("checked") === true) {
		subscribeToNewsletter = true;;
	}
	
	if (isInvalid) {
		return false;
	}
	
	document.getElementById("register-button").disabled = true; 
	// document.getElementById("register-fb-button").disabled = true;
	document.getElementById("register-google-button").disabled = true;

	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/register");
		
	var body = {
		email: userEmail,
		password: userPass,
		subscribeToNewsletter: subscribeToNewsletter,
		acceptTermsAndConditions: acceptTermsAndConditions
	};
	
	$.ajax({
		url: backendUrl,
		type: 'POST',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		data: JSON.stringify(body),
		success: function (response) {
			$("#general-modal").modal();
			document.getElementById('modal-text').innerHTML = "Un email cu instrucțiuni a fost trimis la adresa de email introdusă. Te rugăm să verifici și în SPAM.";
			$("#general-modal").on("hidden.bs.modal", function () {
				var currentLocationPath = window.location.pathname;
				if (currentLocationPath.includes("register")) {
					var urlHomepageRedirect = "./";
					window.location.replace(urlHomepageRedirect);
				} else {
					location.reload();
				}
			});
		},
		error: function(error) {
			if (error.responseJSON) {
				if (error.responseJSON.control) {
					var errorDescription = error.responseJSON.control.errorDescription;
					errorRegister.innerHTML = errorDescription;
				} else {
					errorRegister.innerHTML = "A apărut o eroare. Te rugăm să încerci din nou mai târziu.";
				}
			} else {
				errorRegister.innerHTML = "A apărut o eroare. Te rugăm să încerci din nou mai târziu.";
			}
			// if (error.status == 401) {
				// errorRegister.innerHTML = "Datele de autentificare sunt incorecte.";
			// } else {
				// errorRegister.innerHTML = "A apărut o eroare. Te rugăm să încerci din nou mai târziu.";
			// }
		
			document.getElementById("register-button").disabled = false;
			// document.getElementById("register-fb-button").disabled = false;
			document.getElementById("register-google-button").disabled = false;
		}
	});
	
}

// token can be empty, if user changes password when logged in. token exists only when user forgot password.
function changePass(e, token) {
	e.preventDefault();

	var userPass = document.getElementById("reset-pass").value.trim();
	var userConfPass = document.getElementById("reset-confirm-pass").value.trim();
	
	var errorReset = document.getElementById('error-reset');
	
	var isInvalid = false;
	
	const passMin = 7;
	
	if (!userPass) {
		errorReset.innerHTML = "Câmp obligatoriu";
		isInvalid = true;
	} else if (userPass.length < passMin) {
		errorReset.innerHTML = "Parola trebuie să fie de minim 7 caractere.";
		isInvalid = true;
	} else if (!validatePass(userPass)) {
		errorReset.innerHTML = "Parola trebuie să conțină litere și cifre.";
		isInvalid = true;
	} else if (userPass != userConfPass) {
		errorReset.innerHTML = "Cele două parole nu se potrivesc.";
		isInvalid = true;
	}
	
	if (isInvalid) {
		return false;
	}
	
	document.getElementById("reset-button").disabled = true; 
	
	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/change");
		
	var body = {
		password: userPass,
		passwordResetToken: token
	};
	
	$.ajax({
		url: backendUrl,
		type: 'PUT',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		data: JSON.stringify(body),
		success: function (response) {
			$("#general-modal").modal();
			document.getElementById('modal-text').innerHTML = "Password was successfully changed.";
			$("#general-modal").on("hidden.bs.modal", function () {
				var jwtToken = getUField("ut");
				if (!jwtToken) {
					var urlLoginRedirect = "./login.html";
					window.location.replace(urlLoginRedirect);
				} else {
					var urlUserRedirect = "./user.html";
					window.location.replace(urlUserRedirect);
				}
			});
		},
		error: function(error) {
			if (error.responseJSON) {
				if (error.responseJSON.control) {
					var errorDescription = error.responseJSON.control.errorDescription;
					errorReset.innerHTML = errorDescription;
				} else {
					errorReset.innerHTML = "Parola nu a putut fi schimbată. Te rugăm să încerci din nou mai târziu.";
				}
			} else {
				errorReset.innerHTML = "Parola nu a putut fi schimbată. Te rugăm să încerci din nou mai târziu.";
			}
			// if (error.status == 401) {
				// errorReset.innerHTML = "Datele de autentificare sunt incorecte.";
			// } else {
				// errorReset.innerHTML = "Parola nu a putut fi schimbată. Te rugăm să încerci din nou mai târziu.";
			// }
		
			document.getElementById("reset-button").disabled = false;			
		}
	});
	
}

// send email with password reset instructions
function resetPass(e) {
	e.preventDefault();
	
	var email = document.getElementById("forgot-email").value.trim();
	
	var errorForgot = document.getElementById('error-forgot-email');

	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/reset");
	
	document.getElementById("forgot-button").disabled = true; 
		
	var body = {
		email: email
	};
	
	$.ajax({
		url: backendUrl,
		type: 'PUT',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		data: JSON.stringify(body),
		success: function (response) {
			$("#general-modal").modal();
			document.getElementById('modal-text').innerHTML = "Un email cu instrucțiuni a fost trimis la adresa de email introdusă.";
			$("#general-modal").on("hidden.bs.modal", function () {
				var urlHomepageRedirect = "./";
				window.location.replace(urlHomepageRedirect);
			});
		},
		error: function(error) {
			if (error.responseJSON) {
				if (error.responseJSON.control) {
					var errorDescription = error.responseJSON.control.errorDescription;
					errorForgot.innerHTML = errorDescription;
				} else {
					errorForgot.innerHTML = "Parola nu a putut fi schimbată. Te rugăm să încerci din nou mai târziu.";
				}
			} else {
				errorForgot.innerHTML = "Parola nu a putut fi schimbată. Te rugăm să încerci din nou mai târziu.";
			}
			
			document.getElementById("forgot-button").disabled = false; 
					
		}
	});

}

function verifyRegister(token) {

	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/verifyregister");
		
	var body = {
		emailConfirmationToken: token
	};
	
	$.ajax({
		url: backendUrl,
		type: 'PUT',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		data: JSON.stringify(body),
		success: function (response) {
			$("#general-modal").modal();
			document.getElementById('modal-text').innerHTML = "The email address was successfully confirmed. You can log in.";
		},
		error: function(error) {
			$("#general-modal").modal();
			document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation-triangle fa-3x"></i>';
			if (error.responseJSON) {
				if (error.responseJSON.control) {
					var errorDescription = error.responseJSON.control.errorDescription;
					document.getElementById('modal-text').innerHTML = errorDescription;
				} else {
					document.getElementById('modal-text').innerHTML = "The email address could not be confirmed.";
				}
			} else {
				document.getElementById('modal-text').innerHTML = "The email address could not be confirmed.";
			}
			// document.getElementById('general-modal-footer').innerHTML = 
					// '<div class="left-side">'
					// +'	<button type="button" class="btn btn-default btn-simple" data-dismiss="modal">Ok</button>'
					// +'</div>'
					// +'<div class="divider"></div>'
					// +'<div class="right-side">'
					// +'	<button type="button" class="btn btn-warning btn-simple" data-dismiss="modal" style="color: orange;" onclick="resendConfirmationEmail(&quot;'+token+'&quot;)">Retrimite email de confirmare</button>'
					// +'</div>';
					
		}
	});

}

function verifyReset(token) {

	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/verifyreset");
		
	var body = {
		passwordResetToken: token
	};
	
	$.ajax({
		url: backendUrl,
		type: 'PUT',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		data: JSON.stringify(body),
		success: function (response) {

		},
		error: function(error) {
			$("#general-modal").modal();
			if (error.responseJSON) {
				if (error.responseJSON.control) {
					var errorDescription = error.responseJSON.control.errorDescription;
					document.getElementById('modal-text').innerHTML = errorDescription;
				} else {
					document.getElementById('modal-text').innerHTML = "Error.";
				}
			} else {
				document.getElementById('modal-text').innerHTML = "Error.";
			}
			$("#general-modal").on("hidden.bs.modal", function () {
				var jwtToken = getUField("ut");
				var urlLoginRedirect = "./login.html";
				window.location.replace(urlLoginRedirect);
			});
					
		}
	});

}

// either email or token
function resendConfirmationEmail(email, token) {
	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/resendconfirmation");
		
	var body = {
		email: email,
		emailConfirmationToken: token
	};
	
	$.ajax({
		url: backendUrl,
		type: 'PUT',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		data: JSON.stringify(body),
		success: function (response) {
			$("#general-modal").modal();
			document.getElementById('modal-text').innerHTML = "Un email cu instrucțiuni a fost trimis la adresa de email introdusă. Te rugăm să verifici și în SPAM.";
		},
		
		error: function(error) {
			$("#general-modal").modal();
			document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation-triangle fa-3x"></i>';
			if (error.responseJSON) {
				if (error.responseJSON.control) {
					var errorDescription = error.responseJSON.control.errorDescription;
					document.getElementById('modal-text').innerHTML = errorDescription;
				} else {
					document.getElementById('modal-text').innerHTML = "Email-ul de confirmare nu a putut fi trimis.";
				}
			} else {
				document.getElementById('modal-text').innerHTML = "Email-ul de confirmare nu a putut fi trimis.";
			}
			document.getElementById('general-modal-footer').innerHTML = '<button type="button" class="btn btn-default btn-simple" data-dismiss="modal">OK</button>';
					
		}
	});
}

function logout() {
	localStorage.removeItem("u");
	/* Facebook SDK is disabled (login not currently offered), so FB is not defined — skip the FB logout check.
	FB.getLoginStatus(function(response) {
        if (response.status === 'connected') {
            fbLogout();
        }
    });
	*/
	var urlHomepageRedirect = "./";
	window.location.replace(urlHomepageRedirect);
}

function validateEmail(email) {
    const re = /^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$/;
    return re.test(String(email));
}

function validatePass(pass) {
	var lettersNumbers = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{7,22}$/;
	return lettersNumbers.test(String(pass));
}

// Login is no longer required to start a review — the user can write it first and is
// only asked to log in/register when they try to post it (see postReview() in review.html).
function goToAddReview(el, facultyId) {
	increaseReviewStatistic(0);
	if (typeof gtag === 'function') {
		gtag('event', 'add_review', {
		  'step': '0',
		  'page_name': el.baseURI
		});
	}

	var addReviewRedirectUrl = "./review.html?faculty=" + facultyId;
	location.href = addReviewRedirectUrl;
}

function goToAskQuestion(el, facultyId) {
	var jwtToken = getUField("ut");
	if (!jwtToken) {
		el.setAttribute("data-toggle", 'modal');
		el.setAttribute("data-target", '#login-modal');
		return false;
	}

	var askQuestionRedirectUrl = "./ask-question.html?faculty=" + facultyId;
	location.href = askQuestionRedirectUrl;
}

function addToCompare(button, facultyId, hasText) {

	var icon = $(button).children()[0];
	
	var compareIcon = "fa-plus-circle";
	var compareCheckIcon = "fa-check-circle";
	
	if ($(icon).hasClass(compareCheckIcon)) {
		var urlComparisonRedirect = "./comparison.html";
		location.href = urlComparisonRedirect;
		return;
	}
	
	$(icon).removeClass(compareIcon);
	$(icon).addClass(compareCheckIcon);
	if (hasText) {
		icon.nextSibling.outerHTML = "<span data-i18n-key='see-comparison'>Vezi Comparaţii</span>";
		translateElement(icon.nextSibling);
	}

	showAddedToCompareTooltip(button);

	if (typeof(Storage) !== "undefined") {
		var facultyIdsString = localStorage.getItem("fci");
		var facultyIds;
		if (facultyIdsString) {
			facultyIds = facultyIdsString.split(",");
		} else {
			facultyIds = [];
		}
		if (facultyIds.length <= 20 && !facultyIds.includes(facultyId)) {
			facultyIds.push(facultyId);
		}
		localStorage.setItem("fci", facultyIds);
	} else {
	  // Sorry! No Web Storage support..
	}

}

// Shown once, right after a faculty is added to comparisons — the button itself
// only swaps an icon (and sometimes a text label), so this is often the only
// feedback telling the user the click registered and what a second click does.
// Appended to <body> with position:fixed so it escapes any overflow:hidden card
// the button lives in, across all the different layouts that call addToCompare().
function showAddedToCompareTooltip(button) {
	var rect = button.getBoundingClientRect();

	var tooltip = document.createElement("div");
	tooltip.className = "tooltip-inner added-to-compare-tooltip";
	tooltip.setAttribute("data-i18n-key", "compare-added-tooltip");
	tooltip.innerText = translateKey("compare-added-tooltip") || "Facultatea a fost adăugată la comparații. Apasă din nou pentru a le vedea.";
	tooltip.style.position = "fixed";
	tooltip.style.top = (rect.bottom + 8) + "px";
	tooltip.style.left = (rect.left + rect.width / 2) + "px";
	tooltip.style.transform = "translateX(-50%)";
	tooltip.style.zIndex = 2000;
	tooltip.style.maxWidth = "220px";
	tooltip.style.opacity = "0";
	tooltip.style.transition = "opacity 0.25s ease";
	tooltip.style.pointerEvents = "none";

	document.body.appendChild(tooltip);
	requestAnimationFrame(function() {
		tooltip.style.opacity = "1";
	});

	setTimeout(function() {
		tooltip.style.opacity = "0";
		setTimeout(function() {
			if (tooltip.parentNode) {
				tooltip.parentNode.removeChild(tooltip);
			}
		}, 250);
	}, 4000);
}

function addToFavorites(el, facultyId, hasText) {
	
	var jwtToken = getUField("ut");
	if (!jwtToken) {
		el.setAttribute("data-toggle", 'modal');
		el.setAttribute("data-target", '#login-modal');
		// document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation fa-2x"></i>';
		// document.getElementById('modal-text').innerHTML = "Este nevoie de autentificare pentru a salva facultăți favorite.";
		// document.getElementById('general-modal-footer').innerHTML = 
					// '<div class="left-side">'
					// +'	<button type="button" class="btn btn-default btn-simple" data-dismiss="modal">Mai târziu</button>'
					// +'</div>'
					// +'<div class="divider"></div>'
					// +'<div class="right-side">'
					// +'	<button type="button" class="btn btn-warning btn-simple" data-dismiss="modal" data-toggle="modal" data-target="#login-modal" style="color: orange;">Autentificare</button>'
					// +'</div>';
		return false;
	}
	
	var icon = $(el).children()[0];
	var regularIcon = "far";
	var solidIcon = "fas";
	
	// or check using localstorage
	var favorite;
	if ($(icon).hasClass(regularIcon)) {
		favorite = true;
	} else {
		favorite = false; // remove from favorites
	}
	
	$(el).addClass("fa-disabled");
	
	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/favorite/" + facultyId);
	
	var body = {
		addFavoriteFaculty: favorite
	};
	
	$.ajax({
		url: backendUrl,
		type: 'PUT',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		data: JSON.stringify(body),
		success: function (data) {
			var savedFacultiesIds = getUField("usf");
			if (!savedFacultiesIds) {
				savedFacultiesIds = [];
			}
			if ($(icon).hasClass(regularIcon)) {
				$(icon).removeClass(regularIcon);
				$(icon).addClass(solidIcon);
				if (hasText) {
					icon.nextSibling.innerHTML = "Salvată";
				}
				savedFacultiesIds.push(facultyId);
				setUField("usf", savedFacultiesIds);
			} else {
				$(icon).removeClass(solidIcon);
				$(icon).addClass(regularIcon);
				if (hasText) {
					icon.nextSibling.innerHTML = "Salvează";
				}
				savedFacultiesIds = savedFacultiesIds.filter(function(value, index, arr){
					return facultyId != value;
				});
				setUField("usf", savedFacultiesIds);
			}
		},
		error: function(error) {
			checkLoggedInUser(error.status);
			$("#general-modal").modal();
			document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation-triangle fa-3x"></i>';
			if (error.responseJSON) {
				if (error.responseJSON.control) {
					var errorDescription = error.responseJSON.control.errorDescription;
					document.getElementById('modal-text').innerHTML = errorDescription;
				} else {
					document.getElementById('modal-text').innerHTML = "Facultatea nu a putut fi salvată";
				}
			} else {
				document.getElementById('modal-text').innerHTML = "Facultatea nu a putut fi salvată";
			}
		},
		complete: function (data) {
			$(el).removeClass("fa-disabled");
		}
	});
}

function upvoteReview(el) {
	
	var jwtToken = getUField("ut");
	if (!jwtToken) {
		el.setAttribute("data-toggle", 'modal');
		el.setAttribute("data-target", '#login-modal');
		// document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation fa-2x"></i>';
		// document.getElementById('modal-text').innerHTML = "Este nevoie de autentificare pentru a vota evaluări.";
		// document.getElementById('general-modal-footer').innerHTML = 
					// '<div class="left-side">'
					// +'	<button type="button" class="btn btn-default btn-simple" data-dismiss="modal">Mai târziu</button>'
					// +'</div>'
					// +'<div class="divider"></div>'
					// +'<div class="right-side">'
					// +'	<button type="button" class="btn btn-warning btn-simple" data-dismiss="modal" data-toggle="modal" data-target="#login-modal" style="color: orange;">Autentificare</button>'
					// +'</div>';
		return false;
	}

	var regularIcon = "far";
	var solidIcon = "fas";
	
	// or check using localstorage
	var upvote;
	if ($(el).hasClass(regularIcon)) {
		upvote = true;
	} else {
		upvote = false; // take back the vote
	}
	
	var upvotesValue = parseInt(el.nextSibling.innerHTML);

	var reviewId = $(el).parent().parent().parent().attr("data-reviewid");
	
	var backendUrl = new URL(backendUrlRoot + "/v1/review/" + reviewId + "/upvote");
	
	$(el).addClass("fa-disabled");
	
	var body = {
		upvote: upvote
	};
	
	$.ajax({
		url: backendUrl,
		type: 'PUT',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		data: JSON.stringify(body),
		success: function (data) {
			var upvotedReviewsIds = getUField("ulr");
			if (!upvotedReviewsIds) {
				upvotedReviewsIds = [];
			}
			if ($(el).hasClass(regularIcon)) {
				$(el).removeClass(regularIcon);
				$(el).addClass(solidIcon);
				el.nextSibling.innerHTML = (upvotesValue + 1);
				upvotedReviewsIds.push(reviewId);
				setUField("ulr", upvotedReviewsIds);
			} else {
				$(el).removeClass(solidIcon);
				$(el).addClass(regularIcon);
				el.nextSibling.innerHTML = (upvotesValue - 1);
				upvotedReviewsIds = upvotedReviewsIds.filter(function(value, index, arr){
					return reviewId != value;
				});
				setUField("ulr", upvotedReviewsIds);
			}
		},
		error: function(error) {
			if (checkLoggedInUser(error.status)) {
				$("#general-modal").modal();
				document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation-triangle fa-3x"></i>';
				document.getElementById('modal-text').innerHTML = "Evaluarea nu a putut fi votată";
			}
		},
		complete: function(data) {
			$(el).removeClass("fa-disabled");
		}
	});

}

function reportReview(el) {

	var regularIcon = "far";
	var solidIcon = "fas";
	
	if ($(el).hasClass(regularIcon)) {

		var reviewId = $(el).parent().parent().parent().attr("data-reviewid");
		
		var backendUrl = new URL(backendUrlRoot + "/v1/review/" + reviewId + "/report");
		$.ajax({
			url: backendUrl,
			type: 'PUT',
			dataType: 'json',
			contentType: 'application/json',
			crossDomain: true,
			success: function () {
				$("#general-modal").modal();
				document.getElementById('modal-text').innerHTML = "Evaluarea a fost raportată";
				$(el).removeClass(regularIcon);
				$(el).addClass(solidIcon);
			},
			error: function(error) {
				if (checkLoggedInUser(error.status)) {
					$("#general-modal").modal();
					document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation-triangle fa-3x"></i>';
					document.getElementById('modal-text').innerHTML = "Evaluarea nu a putut fi raportată";
				}
			}
		});
	
	}
}

function addToNotifications(el, facultyId, i) {
	
	var allowNotification = false;
	if ($("#allow-notification-" + i).prop("checked") === false) { // if it was not previously checked, it was checked now
		allowNotification = true;;
	}
	
	$(el).addClass("fa-disabled");
	
	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/notification/" + facultyId);
	
	var body = {
		allowNotification: allowNotification
	};
	
	$.ajax({
		url: backendUrl,
		type: 'PUT',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		data: JSON.stringify(body),
		success: function (data) {
			
		},
		error: function(error) {
			checkLoggedInUser(error.status);
			$("#general-modal").modal();
			document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation-triangle fa-3x"></i>';
			if (error.responseJSON) {
				if (error.responseJSON.control) {
					var errorDescription = error.responseJSON.control.errorDescription;
					document.getElementById('modal-text').innerHTML = errorDescription;
				} else {
					document.getElementById('modal-text').innerHTML = "Notificările nu au putut fi activate.";
				}
			} else {
				document.getElementById('modal-text').innerHTML = "Notificările nu au putut fi activate.";
			}
		},
		complete: function (data) {
			$(el).removeClass("fa-disabled");
		}
	});
	
}

function addCompanyToFavorites(el, companyId, hasText) {

	var jwtToken = getUField("ut");
	if (!jwtToken) {
		el.setAttribute("data-toggle", 'modal');
		el.setAttribute("data-target", '#login-modal');
		return false;
	}

	var icon = $(el).children()[0];
	var regularIcon = "far";
	var solidIcon = "fas";

	// or check using localstorage
	var favorite;
	if ($(icon).hasClass(regularIcon)) {
		favorite = true;
	} else {
		favorite = false; // remove from favorites
	}

	$(el).addClass("fa-disabled");

	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/favorite/company/" + companyId);

	var body = {
		addFavoriteCompany: favorite
	};

	$.ajax({
		url: backendUrl,
		type: 'PUT',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		data: JSON.stringify(body),
		success: function (data) {
			var savedCompaniesIds = getUField("usc");
			if (!savedCompaniesIds) {
				savedCompaniesIds = [];
			}
			if ($(icon).hasClass(regularIcon)) {
				$(icon).removeClass(regularIcon);
				$(icon).addClass(solidIcon);
				if (hasText) {
					icon.nextSibling.innerHTML = "Salvată";
				}
				savedCompaniesIds.push(companyId);
				setUField("usc", savedCompaniesIds);
			} else {
				$(icon).removeClass(solidIcon);
				$(icon).addClass(regularIcon);
				if (hasText) {
					icon.nextSibling.innerHTML = "Salvează";
				}
				savedCompaniesIds = savedCompaniesIds.filter(function(value, index, arr){
					return companyId != value;
				});
				setUField("usc", savedCompaniesIds);
			}
		},
		error: function(error) {
			checkLoggedInUser(error.status);
			$("#general-modal").modal();
			document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation-triangle fa-3x"></i>';
			if (error.responseJSON) {
				if (error.responseJSON.control) {
					var errorDescription = error.responseJSON.control.errorDescription;
					document.getElementById('modal-text').innerHTML = errorDescription;
				} else {
					document.getElementById('modal-text').innerHTML = "Compania nu a putut fi salvată";
				}
			} else {
				document.getElementById('modal-text').innerHTML = "Compania nu a putut fi salvată";
			}
		},
		complete: function (data) {
			$(el).removeClass("fa-disabled");
		}
	});
}

function initiateAutocomplete() {

	let inputValue = '';
	let showNoResults = false;

	const autocomplete = document.querySelector('#search-fieldset');
	const input = document.querySelector('#search-input');
	<!-- const noResults = document.querySelector('#no-results'); -->

	new Autocomplete('#search-fieldset', {
	
		search: input => {
			var backendUrl = new URL(backendUrlRoot + "/v1/faculty");
			if (input) {
				backendUrl.searchParams.append("searchBy", input);
				backendUrl.searchParams.append("orderBy", "viewsCount,desc");
			}
			var numberOfSuggestions = 7;
			backendUrl.searchParams.append("limit", numberOfSuggestions);

			return new Promise(resolve => {
			  if (input.length < 3) {
				return resolve([])
			  }

			  fetch(backendUrl, {
			    method: "GET",
                headers: {
                  "Code": window.location.href.includes("unistart.lt") ? "LT" : "RO",
                }
			  })
				.then(response => response.json())
				.then(data => {
				  const results = data.result.faculties.map((result) => {
					return result;
				  })
				  resolve(results)
				})
			})
		},
		renderResult: (result, props) => {
			var logoSrc = backendUrlRoot + "/v1/faculty/" + result.facultyId + "/logo";
			return ' <li ' + props + ' style="display: flex; align-items: center;">'
				+ '<img src="' + logoSrc + '" style="height: 32px; width: 32px; object-fit: contain; margin-right: 10px; flex-shrink: 0;">'
				+ '<div><div><b>' + escapeHtml(result.facultyName) + '</b></div><small class="text-muted">' + escapeHtml(result.universityName) + '</small></div>'
				+ '</li>'
		},
		getResultValue: result => result.facultyName,
		onSubmit: result => {
			location.href = facultyProfileUrl(result);
		}
	
	
	  
	  <!-- onUpdate: (results, selectedIndex) => { -->
		<!-- showNoResults = inputValue && results.length === 0 -->
		
		<!-- if (showNoResults) { -->
		  <!-- autocomplete.classList.add('no-results') -->
		  <!-- input.setAttribute('aria-describedby', 'no-results') -->
		<!-- } else { -->
		  <!-- autocomplete.classList.remove('no-results') -->
		  <!-- input.removeAttribute('aria-describedby') -->
		<!-- } -->
	  <!-- } -->
	})

}

function goToReviews(facultyId) {
	location.href = facultyProfileUrl(facultyId) + "#reviews";
}


function redirectIfNotLoggedIn() {
	var jwtToken = getUField("ut");
	if (!jwtToken) {
		var urlLoginRedirect = "./login.html";
		window.location.replace(urlLoginRedirect);
	}
}

function checkLogin() {
	var jwtToken = getUField("ut");
	if (jwtToken) {
		var urlHomepageRedirect = "./";
		window.location.replace(urlHomepageRedirect);
	}
}

function checkLoggedInUser(status) {
	// login not necessary and not logged in (no jwt) -> ok
	// login not necessary and logged in expired (jwt expired) -> login again
	// login necessary and not logged in (no jwt) -> login
	// login necessary and logged in expired (jwt expired) -> login again
	// login necessary / not necessary and logged in -> ok
	if (status == 401) {
		var jwtToken = getUField("ut");
		if (jwtToken) {
			localStorage.removeItem("u");
			// document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation fa-2x"></i>';
			// document.getElementById('modal-text').innerHTML = "Te rugăm să te autentifici din nou.";
			// document.getElementById('general-modal-footer').innerHTML = '<button type="button" class="btn btn-warning btn-simple" data-dismiss="modal" style="color: orange;">Autentificare</button>';
			
			// $('#general-modal').modal('show');
			// $("#general-modal").on("hidden.bs.modal", function () {
				// var urlHomepageRedirect = "./";
				// window.location.replace(urlHomepageRedirect);
			logout();
			// });
		} else {
			// document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation fa-2x"></i>';
			// document.getElementById('modal-text').innerHTML = "Este nevoie de autentificare pentru a folosi această funcționalitate.";
			// document.getElementById('general-modal-footer').innerHTML = '<button type="button" class="btn btn-warning btn-simple" data-dismiss="modal" style="color: orange;">Autentificare</button>';
			// $('#general-modal').modal('show');
			// $("#general-modal").on("hidden.bs.modal", function () {
				// var urlHomepageRedirect = "./";
				// window.location.replace(urlHomepageRedirect);
			// });
			logout();
		}
		return false;
	}
	return true;
}

function showYears() {
	if(document.querySelectorAll('input[name=status]:checked').length > 0) {
	  var userStatusValue = document.querySelectorAll('input[name=status]:checked')[0].value;
	  var usearYearsComponent = document.getElementById("user-years");
	  var year3 = document.getElementById("year3");
	  var year4 = document.getElementById("year4");
	  
	  if (userStatusValue === 'STUDENT') {
		$("label[for='year3']").css("display", "inline-block");
		$("label[for='year4']").css("display", "inline-block");
		if (usearYearsComponent.style.display === "none") { 
			usearYearsComponent.style.display = "inline";
		}
	  } else if (userStatusValue === 'MASTERAND') {
			$("label[for='year3']").css("display", "none");
			$("label[for='year4']").css("display", "none");
		if (usearYearsComponent.style.display === "none") { 
			usearYearsComponent.style.display = "inline";
		}
	  } else {
			if (usearYearsComponent.style.display === "inline") { 
			usearYearsComponent.style.display = "none";
		}
	  }
	}
} 

function showUserWork() {
	if(document.querySelectorAll('input[name=status]:checked').length > 0) {
	  var userStatusValue = document.querySelectorAll('input[name=status]:checked')[0].value;
	  var userWorkComponent = document.getElementById("user-work");
	  
	  if (userStatusValue != 'ELEV') {
		userWorkComponent.style.display = "inline";
	  } else {
		userWorkComponent.style.display = "none";
	  }
	  
	}
}

function showUserDomainInterest() {
	if(document.querySelectorAll('input[name=status]:checked').length > 0) {
	  var userStatusValue = document.querySelectorAll('input[name=status]:checked')[0].value;
	  var userDomainInterestComponent = document.getElementById("user-domain-interest");
	  
	  if (userStatusValue === 'ELEV') {
		userDomainInterestComponent.style.display = "inline";
	  } else {
		userDomainInterestComponent.style.display = "none";
	  }
	  
	}
}

function showUserCityInterest() {
	if(document.querySelectorAll('input[name=status]:checked').length > 0) {
	  var userStatusValue = document.querySelectorAll('input[name=status]:checked')[0].value;
	  var userCityInterestComponent = document.getElementById("user-city-interest");
	  
	  if (userStatusValue === 'ELEV') {
		userCityInterestComponent.style.display = "inline";
	  } else {
		userCityInterestComponent.style.display = "none";
	  }
	  
	}
}

function increaseReviewStatistic(step) {

	var backendUrl = new URL(backendUrlRoot + "/v1/analytics/rev");
	backendUrl.searchParams.append("s", step);

	$.ajax({
		url: backendUrl,
		type: 'PUT',
		dataType: 'json',
		contentType: 'application/json',
		crossDomain: true,
		success: function () {
		},
		error: function(error) {
		}
	});
}

function setBusinessText() {
	var backendUrl = new URL(backendUrlRoot + "/v1/config/business");
	$.ajax({
		url: backendUrl,
		type: "GET",
		success: function(response) {
			if (response.control.errorCode === 0) {
				if (document.getElementById('businessTextId') !== null) {
					document.getElementById("businessTextId").innerHTML = response.result.configValue;
				}
				if (document.getElementById('businessTextId1') !== null) {
					document.getElementById("businessTextId1").innerHTML = response.result.configValue;
				}
				if (document.getElementById('businessTextId2') !== null) {
					document.getElementById("businessTextId2").innerHTML = response.result.configValue;
				}
			} else {
				console.log(response.control.errorDescription);
			}
		},
		error: function(error) {
		}
	});

}

function getDomainsForHomePage() {
    const currentUrl = window.location.href;
    var domainsString =
                '<li><input type="checkbox" name="domeniu" id="d1" value="Științe Economice"><label for="d1"><span data-i18n-key="domain-economy">Științe Economice</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d2" value="Științe Inginerești"><label for="d2"><span data-i18n-key="domain-politics">Științe Inginerești</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d3" value="Științe Politice și Comunicare"><label for="d3"><span data-i18n-key="domain-politics">Științe Politice și Comunicare</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d4" value="Științe Umaniste"><label for="d4"><span data-i18n-key="domain-humanities">Științe Umaniste</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d5" value="Științe Juridice"><label for="d5"><span data-i18n-key="domain-judicial">Științe Juridice</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d6" value="Medicină"><label for="d6"><span data-i18n-key="domain-medicine">Medicină</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d7" value="Științe ale Naturii"><label for="d7"><span data-i18n-key="domain-nature">Științe ale Naturii</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d8" value="Arte Arhitectură și Urbanism"><label for="d8"><span data-i18n-key="domain-art-v2">Arte, Arhitectură și Urbanism</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d9" value="Științe Exacte"><label for="d9"><span data-i18n-key="domain-exact">Științe Exacte</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d10" value="Științe Sociale și Psihologie"><label for="d10"><span data-i18n-key="domain-social">Științe Sociale și Psihologie</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d11" value="Științe Agricole"><label for="d11"><span data-i18n-key="domain-agriculture">Științe Agricole</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d12" value="Ştiința Sportului şi Educației Fizice"><label for="d12"><span data-i18n-key="domain-sports">Știința Sportului și Educației Fizice</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d13" value="Ştiinţe Militare Informaţii şi Ordine publică"><label for="d13"><span data-i18n-key="domain-military-v2">Științe Militare și de Informații</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d14" value="Teologie"><label for="d14"><span data-i18n-key="domain-theology">Teologie</span></label></li>';

    if (currentUrl.includes("unistart.ro")) {
        domainsString =
                '<li><input type="checkbox" name="domeniu" id="d1" value="Științe Economice"><label for="d1"><span data-i18n-key="domain-economy">Științe Economice</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d2" value="Științe Inginerești"><label for="d2"><span data-i18n-key="domain-politics">Științe Inginerești</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d3" value="Științe Politice și Comunicare"><label for="d3"><span data-i18n-key="domain-politics">Științe Politice și Comunicare</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d4" value="Științe Umaniste"><label for="d4"><span data-i18n-key="domain-humanities">Științe Umaniste</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d5" value="Științe Juridice"><label for="d5"><span data-i18n-key="domain-judicial">Științe Juridice</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d6" value="Medicină"><label for="d6"><span data-i18n-key="domain-medicine">Medicină</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d7" value="Științe ale Naturii"><label for="d7"><span data-i18n-key="domain-nature">Științe ale Naturii</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d8" value="Arte Arhitectură și Urbanism"><label for="d8"><span data-i18n-key="domain-art-v2">Arte, Arhitectură și Urbanism</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d9" value="Științe Exacte"><label for="d9"><span data-i18n-key="domain-exact">Științe Exacte</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d10" value="Științe Sociale și Psihologie"><label for="d10"><span data-i18n-key="domain-social">Științe Sociale și Psihologie</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d11" value="Științe Agricole"><label for="d11"><span data-i18n-key="domain-agriculture">Științe Agricole</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d12" value="Ştiința Sportului şi Educației Fizice"><label for="d12"><span data-i18n-key="domain-sports">Știința Sportului și Educației Fizice</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d13" value="Ştiinţe Militare Informaţii şi Ordine publică"><label for="d13"><span data-i18n-key="domain-military-v2">Științe Militare și de Informații</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d14" value="Teologie"><label for="d14"><span data-i18n-key="domain-theology">Teologie</span></label></li>';

    } else if (currentUrl.includes("unistart.lt")) {
        domainsString =
                '<li><input type="checkbox" name="domeniu" id="d1" value="Ekonomikos mokslai"><label for="d1"><span data-i18n-key="domain-economy">Ekonomikos mokslai</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d2" value="Inžinerijos mokslai"><label for="d2"><span data-i18n-key="domain-politics">Inžinerijos mokslai</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d3" value="Politikos ir komunikacijos mokslai"><label for="d3"><span data-i18n-key="domain-politics">Politikos ir komunikacijos mokslai</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d4" value="Humanitariniai mokslai"><label for="d4"><span data-i18n-key="domain-humanities">Humanitariniai mokslai</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d5" value="Teisės mokslai"><label for="d5"><span data-i18n-key="domain-judicial">Teisės mokslai</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d6" value="Medicina"><label for="d6"><span data-i18n-key="domain-medicine">Medicina</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d7" value="Gamtos mokslai"><label for="d7"><span data-i18n-key="domain-nature">Gamtos mokslai</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d8" value="Menas Architektūra ir Urbanistika"><label for="d8"><span data-i18n-key="domain-art-v2">Menas, Architektūra ir Urbanistika</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d9" value="Tiksliuosius mokslus"><label for="d9"><span data-i18n-key="domain-exact">Tiksliuosius mokslus</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d10" value="Socialiniai mokslai ir psichologija"><label for="d10"><span data-i18n-key="domain-social">Socialiniai mokslai ir psichologija</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d11" value="Žemės ūkio mokslai"><label for="d11"><span data-i18n-key="domain-agriculture">Žemės ūkio mokslai</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d12" value="Sporto ir kūno kultūros mokslai"><label for="d12"><span data-i18n-key="domain-sports">Sporto ir kūno kultūros mokslai</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d13" value="Kariniai mokslai ir žvalgyba"><label for="d13"><span data-i18n-key="domain-military-v2">Kariniai mokslai ir žvalgyba</span></label></li>'+
                '<li><input type="checkbox" name="domeniu" id="d14" value="Teologija"><label for="d14"><span data-i18n-key="domain-theology">Teologija</span></label></li>';

    }
        document.getElementById("domains-select").innerHTML = domainsString;
}

function getDomainsForHomePageFilterDropdown() {
    const currentUrl = window.location.href;
    var domainsString =
                '<select id="top-by-domain" class="selectpicker" data-style="form-control" data-menu-style="" onchange="loadTopFaculties()">'+
                    '<option id= "all" value="Toate">Toate domeniile</option>'+
                    '<option id="d1" value="Științe Economice">Științe Economice</option>'+
                    '<option id="d2" value="Științe Inginerești">Științe Inginerești</option>'+
                    '<option id="d3" value="Științe Politice și Comunicare">Științe Politice și Comunicare</option>'+
                    '<option id="d4" value="Științe Umaniste">Științe Umaniste</option>'+
                    '<option id="d5" value="Științe Juridice">Științe Juridice</option>'+
                    '<option id="d6" value="Medicină">Medicină</option>'+
                    '<option id="d7" value="Științe ale Naturii">Științe ale Naturii</option>'+
                    '<option id="d8" value="Arte Arhitectură și Urbanism">Arte, Arhitectură și Urbanism</option>'+
                    '<option id="d9" value="Științe Exacte">Științe Exacte</option>'+
                    '<option id="d10" value="Științe Sociale și Psihologie">Științe Sociale și Psihologie</option>'+
                    '<option id="d11" value="Științe Agricole">Științe Agricole</option>'+
                    '<option id="d12" value="Ştiinţa Sportului şi Educaţiei Fizice">Știința Sportului şi Educației Fizice</option>'+
                    '<option id="d13" value="Ştiinţe Militare Informaţii şi Ordine publică">Științe Militare şi de Informații</option>'+
                    '<option id="d14" value="Teologie">Teologie</option>'+
                '</select>';

    if (currentUrl.includes("unistart.ro")) {
    var domainsString =
                '<select id="top-by-domain" class="selectpicker" data-style="form-control" data-menu-style="" onchange="loadTopFaculties()">'+
                    '<option id= "all" value="Toate">Toate domeniile</option>'+
                    '<option id="d1" value="Științe Economice">Științe Economice</option>'+
                    '<option id="d2" value="Științe Inginerești">Științe Inginerești</option>'+
                    '<option id="d3" value="Științe Politice și Comunicare">Științe Politice și Comunicare</option>'+
                    '<option id="d4" value="Științe Umaniste">Științe Umaniste</option>'+
                    '<option id="d5" value="Științe Juridice">Științe Juridice</option>'+
                    '<option id="d6" value="Medicină">Medicină</option>'+
                    '<option id="d7" value="Științe ale Naturii">Științe ale Naturii</option>'+
                    '<option id="d8" value="Arte Arhitectură și Urbanism">Arte, Arhitectură și Urbanism</option>'+
                    '<option id="d9" value="Științe Exacte">Științe Exacte</option>'+
                    '<option id="d10" value="Științe Sociale și Psihologie">Științe Sociale și Psihologie</option>'+
                    '<option id="d11" value="Științe Agricole">Științe Agricole</option>'+
                    '<option id="d12" value="Ştiinţa Sportului şi Educaţiei Fizice">Știința Sportului şi Educației Fizice</option>'+
                    '<option id="d13" value="Ştiinţe Militare Informaţii şi Ordine publică">Științe Militare şi de Informații</option>'+
                    '<option id="d14" value="Teologie">Teologie</option>'+
                '</select>';

    } else if (currentUrl.includes("unistart.lt")) {
    var domainsString =
                '<select id="top-by-domain" class="selectpicker" data-style="form-control" data-menu-style="" onchange="loadTopFaculties()">'+
                    '<option id= "all" value="Toate">Visos sritys</option>'+
                    '<option id="d1" value="Ekonomikos mokslai">Ekonomikos mokslai</option>'+
                    '<option id="d2" value="Inžinerijos mokslai">Inžinerijos mokslai</option>'+
                    '<option id="d3" value="Politikos ir komunikacijos mokslai">Politikos ir komunikacijos mokslai</option>'+
                    '<option id="d4" value="Humanitariniai mokslai">Humanitariniai mokslai</option>'+
                    '<option id="d5" value="Teisės mokslai">Teisės mokslai</option>'+
                    '<option id="d6" value="Medicina">Medicina</option>'+
                    '<option id="d7" value="Gamtos mokslai">Gamtos mokslai</option>'+
                    '<option id="d8" value="Menas Architektūra ir Urbanistika">Menas, Architektūra ir Urbanistika</option>'+
                    '<option id="d9" value="Tiksliuosius mokslus">Tiksliuosius mokslus</option>'+
                    '<option id="d10" value="Socialiniai mokslai ir psichologija">Socialiniai mokslai ir psichologija</option>'+
                    '<option id="d11" value="Žemės ūkio mokslai">Žemės ūkio mokslai</option>'+
                    '<option id="d12" value="Sporto ir kūno kultūros mokslai">Sporto ir kūno kultūros mokslai</option>'+
                    '<option id="d13" value="Kariniai mokslai ir žvalgyba">Kariniai mokslai ir žvalgyba</option>'+
                    '<option id="d14" value="Teologija">Teologija</option>'+
                '</select>';

    }
        document.getElementById("dropdown-domains").innerHTML = domainsString;
}

function getDomainsForSearchPage() {

    const currentUrl = window.location.href;
    var domainsString =
                '<label class="checkbox checkbox-azure" for="d1"><input type="checkbox" name="domeniu" value="Științe Economice" id="d1" data-toggle="checkbox"><span data-i18n-key="domain-economy">Științe Economice</span></label>'+
                '<label class="checkbox checkbox-azure" for="d2"><input type="checkbox" name="domeniu" value="Științe Inginerești" id="d2" data-toggle="checkbox"><span data-i18n-key="domain-engineering">Științe Inginerești</span></label>'+
                '<label class="checkbox checkbox-azure" for="d3"><input type="checkbox" name="domeniu" value="Științe Politice și Comunicare" id="d3" data-toggle="checkbox"><span data-i18n-key="domain-politics">Științe Politice și Comunicare</span></label>'+
                '<label class="checkbox checkbox-azure" for="d4"><input type="checkbox" name="domeniu" value="Științe Umaniste" id="d4" data-toggle="checkbox"><span data-i18n-key="domain-humanities">Științe Umaniste</span></label>'+
                '<label class="checkbox checkbox-azure" for="d5"><input type="checkbox" name="domeniu" value="Științe Juridice" id="d5" data-toggle="checkbox"><span data-i18n-key="domain-judicial">Științe Juridice</span></label>'+
                '<label class="checkbox checkbox-azure" for="d6"><input type="checkbox" name="domeniu" value="Medicină" id="d6" data-toggle="checkbox"><span data-i18n-key="domain-medicine">Medicină</span></label>'+
                '<label class="checkbox checkbox-azure" for="d7"><input type="checkbox" name="domeniu" value="Științe ale Naturii" id="d7" data-toggle="checkbox"><span data-i18n-key="domain-nature">Științe ale Naturii</span></label>'+
                '<label class="checkbox checkbox-azure" for="d8"><input type="checkbox" name="domeniu" value="Arte Arhitectură și Urbanism" id="d8" data-toggle="checkbox"><span data-i18n-key="domain-art-v2">Arte, Arhitectură și Urbanism</span></label>'+
                '<label class="checkbox checkbox-azure" for="d9"><input type="checkbox" name="domeniu" value="Științe Exacte" id="d9" data-toggle="checkbox"><span data-i18n-key="domain-exact">Științe Exacte</span></label>'+
                '<label class="checkbox checkbox-azure" for="d10"><input type="checkbox" name="domeniu" value="Științe Sociale și Psihologie" id="d10" data-toggle="checkbox"><span data-i18n-key="domain-social">Științe Sociale și Psihologie</span></label>'+
                '<label class="checkbox checkbox-azure" for="d11"><input type="checkbox" name="domeniu" value="Științe Agricole" id="d11" data-toggle="checkbox"><span data-i18n-key="domain-agriculture">Științe Agricole</span></label>'+
                '<label class="checkbox checkbox-azure" for="d12"><input type="checkbox" name="domeniu" value="Ştiinţa Sportului şi Educaţiei Fizice" id="d12" data-toggle="checkbox"><span data-i18n-key="domain-sports">Știința Sportului și Educației Fizice</span></label>'+
                '<label class="checkbox checkbox-azure" for="d13"><input type="checkbox" name="domeniu" value="Ştiinţe Militare Informaţii şi Ordine publică" id="d13" data-toggle="checkbox"><span data-i18n-key="domain-military-v2">Științe Militare și de Informații</span></label>'+
                '<label class="checkbox checkbox-azure" for="d14"><input type="checkbox" name="domeniu" value="Teologie" id="d14" data-toggle="checkbox"><span data-i18n-key="domain-theology">Teologie</span></label>';


    if (currentUrl.includes("unistart.ro")) {
        domainsString =
                '<label class="checkbox checkbox-azure" for="d1"><input type="checkbox" name="domeniu" value="Științe Economice" id="d1" data-toggle="checkbox"><span data-i18n-key="domain-economy">Științe Economice</span></label>'+
                '<label class="checkbox checkbox-azure" for="d2"><input type="checkbox" name="domeniu" value="Științe Inginerești" id="d2" data-toggle="checkbox"><span data-i18n-key="domain-engineering">Științe Inginerești</span></label>'+
                '<label class="checkbox checkbox-azure" for="d3"><input type="checkbox" name="domeniu" value="Științe Politice și Comunicare" id="d3" data-toggle="checkbox"><span data-i18n-key="domain-politics">Științe Politice și Comunicare</span></label>'+
                '<label class="checkbox checkbox-azure" for="d4"><input type="checkbox" name="domeniu" value="Științe Umaniste" id="d4" data-toggle="checkbox"><span data-i18n-key="domain-humanities">Științe Umaniste</span></label>'+
                '<label class="checkbox checkbox-azure" for="d5"><input type="checkbox" name="domeniu" value="Științe Juridice" id="d5" data-toggle="checkbox"><span data-i18n-key="domain-judicial">Științe Juridice</span></label>'+
                '<label class="checkbox checkbox-azure" for="d6"><input type="checkbox" name="domeniu" value="Medicină" id="d6" data-toggle="checkbox"><span data-i18n-key="domain-medicine">Medicină</span></label>'+
                '<label class="checkbox checkbox-azure" for="d7"><input type="checkbox" name="domeniu" value="Științe ale Naturii" id="d7" data-toggle="checkbox"><span data-i18n-key="domain-nature">Științe ale Naturii</span></label>'+
                '<label class="checkbox checkbox-azure" for="d8"><input type="checkbox" name="domeniu" value="Arte Arhitectură și Urbanism" id="d8" data-toggle="checkbox"><span data-i18n-key="domain-art-v2">Arte, Arhitectură și Urbanism</span></label>'+
                '<label class="checkbox checkbox-azure" for="d9"><input type="checkbox" name="domeniu" value="Științe Exacte" id="d9" data-toggle="checkbox"><span data-i18n-key="domain-exact">Științe Exacte</span></label>'+
                '<label class="checkbox checkbox-azure" for="d10"><input type="checkbox" name="domeniu" value="Științe Sociale și Psihologie" id="d10" data-toggle="checkbox"><span data-i18n-key="domain-social">Științe Sociale și Psihologie</span></label>'+
                '<label class="checkbox checkbox-azure" for="d11"><input type="checkbox" name="domeniu" value="Științe Agricole" id="d11" data-toggle="checkbox"><span data-i18n-key="domain-agriculture">Științe Agricole</span></label>'+
                '<label class="checkbox checkbox-azure" for="d12"><input type="checkbox" name="domeniu" value="Ştiinţa Sportului şi Educaţiei Fizice" id="d12" data-toggle="checkbox"><span data-i18n-key="domain-sports">Știința Sportului și Educației Fizice</span></label>'+
                '<label class="checkbox checkbox-azure" for="d13"><input type="checkbox" name="domeniu" value="Ştiinţe Militare Informaţii şi Ordine publică" id="d13" data-toggle="checkbox"><span data-i18n-key="domain-military-v2">Științe Militare și de Informații</span></label>'+
                '<label class="checkbox checkbox-azure" for="d14"><input type="checkbox" name="domeniu" value="Teologija" id="d14" data-toggle="checkbox"><span data-i18n-key="domain-theology">Teologija</span></label>';

    } else if (currentUrl.includes("unistart.lt")) {
        domainsString =
                '<label class="checkbox checkbox-azure" for="d1"><input type="checkbox" name="domeniu" value="Ekonomikos mokslai" id="d1" data-toggle="checkbox"><span data-i18n-key="domain-economy">Ekonomikos mokslai</span></label>'+
                '<label class="checkbox checkbox-azure" for="d2"><input type="checkbox" name="domeniu" value="Inžinerijos mokslai" id="d2" data-toggle="checkbox"><span data-i18n-key="domain-engineering">Inžinerijos mokslai</span></label>'+
                '<label class="checkbox checkbox-azure" for="d3"><input type="checkbox" name="domeniu" value="Politikos ir komunikacijos mokslai" id="d3" data-toggle="checkbox"><span data-i18n-key="domain-politics">Politikos ir komunikacijos mokslai</span></label>'+
                '<label class="checkbox checkbox-azure" for="d4"><input type="checkbox" name="domeniu" value="Humanitariniai mokslai" id="d4" data-toggle="checkbox"><span data-i18n-key="domain-humanities">Humanitariniai mokslai</span></label>'+
                '<label class="checkbox checkbox-azure" for="d5"><input type="checkbox" name="domeniu" value="Teisės mokslai" id="d5" data-toggle="checkbox"><span data-i18n-key="domain-judicial">Teisės mokslai</span></label>'+
                '<label class="checkbox checkbox-azure" for="d6"><input type="checkbox" name="domeniu" value="Medicina" id="d6" data-toggle="checkbox"><span data-i18n-key="domain-medicine">Medicina</span></label>'+
                '<label class="checkbox checkbox-azure" for="d7"><input type="checkbox" name="domeniu" value="Gamtos mokslai" id="d7" data-toggle="checkbox"><span data-i18n-key="domain-nature">Gamtos mokslai</span></label>'+
                '<label class="checkbox checkbox-azure" for="d8"><input type="checkbox" name="domeniu" value="Menas Architektūra ir Urbanistika" id="d8" data-toggle="checkbox"><span data-i18n-key="domain-art-v2">Menas, Architektūra ir Urbanistika</span></label>'+
                '<label class="checkbox checkbox-azure" for="d9"><input type="checkbox" name="domeniu" value="Tiksliuosius mokslus" id="d9" data-toggle="checkbox"><span data-i18n-key="domain-exact">Tiksliuosius mokslus</span></label>'+
                '<label class="checkbox checkbox-azure" for="d10"><input type="checkbox" name="domeniu" value="Socialiniai mokslai ir psichologija" id="d10" data-toggle="checkbox"><span data-i18n-key="domain-social">Socialiniai mokslai ir psichologija</span></label>'+
                '<label class="checkbox checkbox-azure" for="d11"><input type="checkbox" name="domeniu" value="Žemės ūkio mokslai" id="d11" data-toggle="checkbox"><span data-i18n-key="domain-agriculture">Žemės ūkio mokslai</span></label>'+
                '<label class="checkbox checkbox-azure" for="d12"><input type="checkbox" name="domeniu" value="Sporto ir kūno kultūros mokslai" id="d12" data-toggle="checkbox"><span data-i18n-key="domain-sports">Sporto ir kūno kultūros mokslai</span></label>'+
                '<label class="checkbox checkbox-azure" for="d13"><input type="checkbox" name="domeniu" value="Kariniai mokslai ir žvalgyba" id="d13" data-toggle="checkbox"><span data-i18n-key="domain-military-v2">Kariniai mokslai ir žvalgyba</span></label>'+
                '<label class="checkbox checkbox-azure" for="d14"><input type="checkbox" name="domeniu" value="Teologie" id="d14" data-toggle="checkbox"><span data-i18n-key="domain-theology">Teologie</span></label>';

    }
    document.getElementById("domains-select").innerHTML = domainsString;
}

function getCitiesForHomePage() {
    const currentUrl = window.location.href;
    var citiesString =
                '<li><input type="checkbox" name="oras" id="c1" value="Bucureşti"><label for="c1">Bucureşti</label></li>'+
                '<li><input type="checkbox" name="oras" id="c2" value="Cluj-Napoca; Cluj"><label for="c2">Cluj-Napoca</label></li>'+
                '<li><input type="checkbox" name="oras" id="c3" value="Timişoara; Timiş"><label for="c3">Timişoara</label></li>'+
                '<li><input type="checkbox" name="oras" id="c4" value="Iaşi; Iaşi"><label for="c4">Iaşi</label></li>'+
                '<li><input type="checkbox" name="oras" id="c5" value="Constanţa; Constanţa"><label for="c5">Constanţa</label></li>'+
                '<li><input type="checkbox" name="oras" id="c6" value="Craiova; Dolj"><label for="c6">Craiova</label></li>'+
                '<li><input type="checkbox" name="oras" id="c10" value="Oradea; Bihor"><label for="c10">Oradea</label></li>'+
                '<li><input type="checkbox" name="oras" id="c7" value="Braşov; Braşov"><label for="c7">Braşov</label></li>'+
                '<li><input type="checkbox" name="oras" id="c8" value="Piteşti; Argeş"><label for="c8">Piteşti</label></li>'+
                '<li><input type="checkbox" name="oras" id="c9" value="Târgovişte; Dâmboviţa"><label for="c9">Târgovişte</label></li>'+
                '<li><input type="checkbox" name="oras" id="c11" value="Alba Iulia; Alba"><label for="c11">Alba Iulia</label></li>'+
                '<li><input type="checkbox" name="oras" id="c12" value="Sibiu; Sibiu"><label for="c12">Sibiu</label></li>'+
                '<li><input type="checkbox" name="oras" id="c13" value="Galaţi; Galaţi"><label for="c13">Galaţi</label></li>'+
                '<li><input type="checkbox" name="oras" id="c14" value="Arad; Arad"><label for="c14">Arad</label></li>'+
                '<li><input type="checkbox" name="oras" id="c15" value="Târgu Mureş; Mureş"><label for="c15">Târgu Mureş</label></li>'+
                '<li><input type="checkbox" name="oras" id="c16" value="Suceava; Suceava"><label for="c16">Suceava</label></li>';

    if (currentUrl.includes("unistart.ro")) {
        citiesString =
                '<li><input type="checkbox" name="oras" id="c1" value="Bucureşti"><label for="c1">Bucureşti</label></li>'+
                '<li><input type="checkbox" name="oras" id="c2" value="Cluj-Napoca; Cluj"><label for="c2">Cluj-Napoca</label></li>'+
                '<li><input type="checkbox" name="oras" id="c3" value="Timişoara; Timiş"><label for="c3">Timişoara</label></li>'+
                '<li><input type="checkbox" name="oras" id="c4" value="Iaşi; Iaşi"><label for="c4">Iaşi</label></li>'+
                '<li><input type="checkbox" name="oras" id="c5" value="Constanţa; Constanţa"><label for="c5">Constanţa</label></li>'+
                '<li><input type="checkbox" name="oras" id="c6" value="Craiova; Dolj"><label for="c6">Craiova</label></li>'+
                '<li><input type="checkbox" name="oras" id="c10" value="Oradea; Bihor"><label for="c10">Oradea</label></li>'+
                '<li><input type="checkbox" name="oras" id="c7" value="Braşov; Braşov"><label for="c7">Braşov</label></li>'+
                '<li><input type="checkbox" name="oras" id="c8" value="Piteşti; Argeş"><label for="c8">Piteşti</label></li>'+
                '<li><input type="checkbox" name="oras" id="c9" value="Târgovişte; Dâmboviţa"><label for="c9">Târgovişte</label></li>'+
                '<li><input type="checkbox" name="oras" id="c11" value="Alba Iulia; Alba"><label for="c11">Alba Iulia</label></li>'+
                '<li><input type="checkbox" name="oras" id="c12" value="Sibiu; Sibiu"><label for="c12">Sibiu</label></li>'+
                '<li><input type="checkbox" name="oras" id="c13" value="Galaţi; Galaţi"><label for="c13">Galaţi</label></li>'+
                '<li><input type="checkbox" name="oras" id="c14" value="Arad; Arad"><label for="c14">Arad</label></li>'+
                '<li><input type="checkbox" name="oras" id="c15" value="Târgu Mureş; Mureş"><label for="c15">Târgu Mureş</label></li>'+
                '<li><input type="checkbox" name="oras" id="c16" value="Suceava; Suceava"><label for="c16">Suceava</label></li>';

    } else if (currentUrl.includes("unistart.lt")) {
        citiesString =
                '<li><input type="checkbox" name="oras" id="c1" value="Vilnius"><label for="c1">Vilnius</label></li>'+
                '<li><input type="checkbox" name="oras" id="c2" value="Kaunas"><label for="c2">Kaunas</label></li>'+
                '<li><input type="checkbox" name="oras" id="c3" value="Klaipėda"><label for="c3">Klaipėda</label></li>'+
                '<li><input type="checkbox" name="oras" id="c4" value="Šiauliai"><label for="c4">Šiauliai</label></li>'+
                '<li><input type="checkbox" name="oras" id="c5" value="Panevėžys"><label for="c5">Panevėžys</label></li>';

    }
        document.getElementById("cities-select").innerHTML = citiesString;
}

function getCitiesForHomePageFilterDropdown() {
    const currentUrl = window.location.href;
    var citiesString =
                '<select id="top-by-city" class="selectpicker" data-style="form-control" data-menu-style="" onchange="loadTopFaculties()">'+
                    '<option id= "all" value="Toate">Toate oraşele</option>'+
                    '<option id="c1" value="Bucureşti">Bucureşti</option>'+
                    '<option id="c2" value="Cluj-Napoca; Cluj">Cluj-Napoca</option>'+
                    '<option id="c3" value="Timişoara; Timiş">Timişoara</option>'+
                    '<option id="c4" value="Iaşi; Iaşi">Iaşi</option>'+
                    '<option id="c5" value="Constanţa; Constanţa">Constanţa</option>'+
                    '<option id="c6" value="Craiova; Dolj">Craiova</option>'+
                    '<option id="c10" value="Oradea; Bihor">Oradea</option>'+
                    '<option id="c7" value="Braşov; Braşov">Braşov</option>'+
                    '<option id="c8" value="Piteşti; Argeş">Piteşti</option>'+
                    '<option id="c9" value="Târgovişte; Dâmboviţa">Târgovişte</option>'+
                    '<option id="c11" value="Alba Iulia; Alba">Alba Iulia</option>'+
                    '<option id="c12" value="Sibiu; Sibiu">Sibiu</option>'+
                    '<option id="c13" value="Galaţi; Galaţi">Galaţi</option>'+
                    '<option id="c14" value="Arad; Arad">Arad</option>'+
                    '<option id="c15" value="Târgu Mureş; Mureş">Târgu Mureş</option>'+
                    '<option id="c16" value="Suceava; Suceava">Suceava</option>'+
                    '<option id="c17" value="Alba Iulia; Alba">Alba Iulia</option>'+
                '</select>';

    if (currentUrl.includes("unistart.ro")) {
    var citiesString =
                '<select id="top-by-city" class="selectpicker" data-style="form-control" data-menu-style="" onchange="loadTopFaculties()">'+
                    '<option id= "all" value="Toate">Toate oraşele</option>'+
                    '<option id="c1" value="Bucureşti">Bucureşti</option>'+
                    '<option id="c2" value="Cluj-Napoca; Cluj">Cluj-Napoca</option>'+
                    '<option id="c3" value="Timişoara; Timiş">Timişoara</option>'+
                    '<option id="c4" value="Iaşi; Iaşi">Iaşi</option>'+
                    '<option id="c5" value="Constanţa; Constanţa">Constanţa</option>'+
                    '<option id="c6" value="Craiova; Dolj">Craiova</option>'+
                    '<option id="c10" value="Oradea; Bihor">Oradea</option>'+
                    '<option id="c7" value="Braşov; Braşov">Braşov</option>'+
                    '<option id="c8" value="Piteşti; Argeş">Piteşti</option>'+
                    '<option id="c9" value="Târgovişte; Dâmboviţa">Târgovişte</option>'+
                    '<option id="c11" value="Alba Iulia; Alba">Alba Iulia</option>'+
                    '<option id="c12" value="Sibiu; Sibiu">Sibiu</option>'+
                    '<option id="c13" value="Galaţi; Galaţi">Galaţi</option>'+
                    '<option id="c14" value="Arad; Arad">Arad</option>'+
                    '<option id="c15" value="Târgu Mureş; Mureş">Târgu Mureş</option>'+
                    '<option id="c16" value="Suceava; Suceava">Suceava</option>'+
                    '<option id="c17" value="Alba Iulia; Alba">Alba Iulia</option>'+
                '</select>';

    } else if (currentUrl.includes("unistart.lt")) {
    var citiesString =
                '<select id="top-by-city" class="selectpicker" data-style="form-control" data-menu-style="" onchange="loadTopFaculties()">'+
                    '<option id= "all" value="Toate">Visi miestai</option>'+
                    '<option id="c1" value="Vilnius">Vilnius</option>'+
                    '<option id="c2" value="Kaunas">Kaunas</option>'+
                    '<option id="c3" value="Klaipėda">Klaipėda</option>'+
                    '<option id="c4" value="Šiauliai">Šiauliai</option>'+
                    '<option id="c5" value="Panevėžys">Panevėžys</option>'+
                '</select>';

    }
        document.getElementById("dropdown-cities").innerHTML = citiesString;
}

function getCitiesForSearchPage() {

    const currentUrl = window.location.href;
    var citiesString =
                '<label class="checkbox checkbox-azure" for="c1"><input type="checkbox" name="oras" value="Bucureşti" id="c1" data-toggle="checkbox">Bucureşti</label>'+
                '<label class="checkbox checkbox-azure" for="c2"><input type="checkbox" name="oras" value="Cluj-Napoca; Cluj" id="c2" data-toggle="checkbox">Cluj-Napoca</label>'+
                '<label class="checkbox checkbox-azure" for="c3"><input type="checkbox" name="oras" value="Timişoara; Timiş" id="c3" data-toggle="checkbox">Timişoara</label>'+
                '<label class="checkbox checkbox-azure" for="c4"><input type="checkbox" name="oras" value="Iaşi; Iaşi" id="c4" data-toggle="checkbox">Iaşi</label>'+
                '<label class="checkbox checkbox-azure" for="c5"><input type="checkbox" name="oras" value="Constanţa; Constanţa" id="c5" data-toggle="checkbox">Constanţa</label>'+
                '<label class="checkbox checkbox-azure" for="c6"><input type="checkbox" name="oras" value="Craiova; Dolj" id="c6" data-toggle="checkbox">Craiova</label>'+
                '<label class="checkbox checkbox-azure" for="c10"><input type="checkbox" name="oras" value="Oradea; Bihor" id="c10" data-toggle="checkbox">Oradea</label>'+
                '<label class="checkbox checkbox-azure" for="c7"><input type="checkbox" name="oras" value="Braşov; Braşov" id="c7" data-toggle="checkbox">Braşov</label>'+
                '<label class="checkbox checkbox-azure" for="c8"><input type="checkbox" name="oras" value="Piteşti; Argeş" id="c8" data-toggle="checkbox">Piteşti</label>'+
                '<label class="checkbox checkbox-azure" for="c9"><input type="checkbox" name="oras" value="Târgovişte; Dâmboviţa" id="c9" data-toggle="checkbox">Târgovişte</label>'+
                '<label class="checkbox checkbox-azure" for="c11"><input type="checkbox" name="oras" value="Alba Iulia; Alba" id="c11" data-toggle="checkbox">Alba Iulia</label>'+
                '<label class="checkbox checkbox-azure" for="c12"><input type="checkbox" name="oras" value="Sibiu; Sibiu" id="c12" data-toggle="checkbox">Sibiu</label>'+
                '<label class="checkbox checkbox-azure" for="c13"><input type="checkbox" name="oras" value="Galaţi; Galaţi" id="c13" data-toggle="checkbox">Galaţi</label>'+
                '<label class="checkbox checkbox-azure" for="c14"><input type="checkbox" name="oras" value="Arad; Arad" id="c14" data-toggle="checkbox">Arad</label>'+
                '<label class="checkbox checkbox-azure" for="c15"><input type="checkbox" name="oras" value="Târgu Mureş; Mureş" id="c15" data-toggle="checkbox">Târgu Mureş</label>'+
                '<label class="checkbox checkbox-azure" for="c16"><input type="checkbox" name="oras" value="Suceava; Suceava" id="c16" data-toggle="checkbox">Suceava</label>';


    if (currentUrl.includes("unistart.ro")) {
        citiesString =
            '<label class="checkbox checkbox-azure" for="c1"><input type="checkbox" name="oras" value="Bucureşti" id="c1" data-toggle="checkbox">Bucureşti</label>'+
            '<label class="checkbox checkbox-azure" for="c2"><input type="checkbox" name="oras" value="Cluj-Napoca; Cluj" id="c2" data-toggle="checkbox">Cluj-Napoca</label>'+
            '<label class="checkbox checkbox-azure" for="c3"><input type="checkbox" name="oras" value="Timişoara; Timiş" id="c3" data-toggle="checkbox">Timişoara</label>'+
            '<label class="checkbox checkbox-azure" for="c4"><input type="checkbox" name="oras" value="Iaşi; Iaşi" id="c4" data-toggle="checkbox">Iaşi</label>'+
            '<label class="checkbox checkbox-azure" for="c5"><input type="checkbox" name="oras" value="Constanţa; Constanţa" id="c5" data-toggle="checkbox">Constanţa</label>'+
            '<label class="checkbox checkbox-azure" for="c6"><input type="checkbox" name="oras" value="Craiova; Dolj" id="c6" data-toggle="checkbox">Craiova</label>'+
            '<label class="checkbox checkbox-azure" for="c10"><input type="checkbox" name="oras" value="Oradea; Bihor" id="c10" data-toggle="checkbox">Oradea</label>'+
            '<label class="checkbox checkbox-azure" for="c7"><input type="checkbox" name="oras" value="Braşov; Braşov" id="c7" data-toggle="checkbox">Braşov</label>'+
            '<label class="checkbox checkbox-azure" for="c8"><input type="checkbox" name="oras" value="Piteşti; Argeş" id="c8" data-toggle="checkbox">Piteşti</label>'+
            '<label class="checkbox checkbox-azure" for="c9"><input type="checkbox" name="oras" value="Târgovişte; Dâmboviţa" id="c9" data-toggle="checkbox">Târgovişte</label>'+
            '<label class="checkbox checkbox-azure" for="c11"><input type="checkbox" name="oras" value="Alba Iulia; Alba" id="c11" data-toggle="checkbox">Alba Iulia</label>'+
            '<label class="checkbox checkbox-azure" for="c12"><input type="checkbox" name="oras" value="Sibiu; Sibiu" id="c12" data-toggle="checkbox">Sibiu</label>'+
            '<label class="checkbox checkbox-azure" for="c13"><input type="checkbox" name="oras" value="Galaţi; Galaţi" id="c13" data-toggle="checkbox">Galaţi</label>'+
            '<label class="checkbox checkbox-azure" for="c14"><input type="checkbox" name="oras" value="Arad; Arad" id="c14" data-toggle="checkbox">Arad</label>'+
            '<label class="checkbox checkbox-azure" for="c15"><input type="checkbox" name="oras" value="Târgu Mureş; Mureş" id="c15" data-toggle="checkbox">Târgu Mureş</label>'+
            '<label class="checkbox checkbox-azure" for="c16"><input type="checkbox" name="oras" value="Suceava; Suceava" id="c16" data-toggle="checkbox">Suceava</label>';

    } else if (currentUrl.includes("unistart.lt")) {
        citiesString =
            '<label class="checkbox checkbox-azure" for="c1"><input type="checkbox" name="oras" value="Vilnius" id="c1" data-toggle="checkbox">Vilnius</label>'+
            '<label class="checkbox checkbox-azure" for="c2"><input type="checkbox" name="oras" value="Kaunas" id="c2" data-toggle="checkbox">Kaunas</label>'+
            '<label class="checkbox checkbox-azure" for="c3"><input type="checkbox" name="oras" value="Klaipėda" id="c3" data-toggle="checkbox">Klaipėda</label>'+
            '<label class="checkbox checkbox-azure" for="c4"><input type="checkbox" name="oras" value="Šiauliai" id="c4" data-toggle="checkbox">Šiauliai</label>'+
            '<label class="checkbox checkbox-azure" for="c5"><input type="checkbox" name="oras" value="Panevėžys" id="c5" data-toggle="checkbox">Panevėžys</label>';

    }
    document.getElementById("cities-select").innerHTML = citiesString;
}

// The locale our app first shows
const defaultLocale = "en";
// Gets filled with active locale translations
let translations = {};
// When the page content is ready...
document.addEventListener("DOMContentLoaded", () => {
    const currentUrl = window.location.href;
    if (!localStorage.getItem("locale")) {
        if (currentUrl.includes("unistart.ro")) {
            localStorage.setItem("locale", "ro");
        } else if (currentUrl.includes("unistart.lt")) {
            localStorage.setItem("locale", "lt");
        }
    }
    setLocale(localStorage.getItem("locale"));

});
// Load translations for the given locale and translate
// the page to this locale
async function setLocale(newLocale) {
  const newTranslations =
    await fetchTranslationsFor(newLocale);
  translations = newTranslations;
  translatePage();
}
// Retrieve translations JSON object for the given
// locale over the network
async function fetchTranslationsFor(newLocale) {
   if (newLocale === "ro") {
    return ro_json;
   } else if (newLocale === "en") {
    return en_json;
   } else if (newLocale === "lt") {
    return lt_json;
   } else {
    return en_json;
   }
}

// Replace the inner text of each element that has a
// data-i18n-key attribute with the translation corresponding
// to its data-i18n-key
function translatePage() {
  document
    .querySelectorAll("[data-i18n-key]")
    .forEach(translateElement);
}
// Replace the inner text of the given HTML element
// with the translation in the active locale,
// corresponding to the element's data-i18n-key
function translateElement(element) {
  const key = element.getAttribute("data-i18n-key");
  const translation = translations[key];
  element.innerText = translation;
}

function translateKey(key) {
    return translations[key];
}

function getLocaleFlags() {
    const currentUrl = window.location.href;
    if (currentUrl.includes("unistart.ro")) {
        return '<a id="locale-flags-ro" class="img-ro-flag" style="display: inline-block" onclick="changeLocale(&quot;ro&quot;)"></a><a id="locale-flags-uk" class="img-uk-flag" style="display: inline-block" onclick="changeLocale((&quot;en&quot;))"></a>';
    } else if (currentUrl.includes("unistart.lt")) {
        return '<a id="locale-flags-lt" class="img-lt-flag" style="display: inline-block" onclick="changeLocale(&quot;lt&quot;)"></a><a id="locale-flags-uk" class="img-uk-flag" style="display: inline-block" onclick="changeLocale((&quot;en&quot;))"></a>';
    } else {
        return '<a id="locale-flags-ro" class="img-ro-flag" style="display: inline-block" onclick="changeLocale(&quot;ro&quot;)"></a><a id="locale-flags-uk" class="img-uk-flag" style="display: inline-block" onclick="changeLocale((&quot;en&quot;))"></a>';
    }
}

function changeLocale(locale) {
    localStorage.setItem("locale", locale);
    setLocale(localStorage.getItem("locale"));
}

function getLoginModal() {
    var loginModal = '							<div class="close close-login" data-dismiss="modal">'+
                     '								<i class="fa fa-close"></i>'+
                     '							</div>'+
                     '							<h3 class="title" data-i18n-key="login-header">Autentificare</h3>'+
                     '							<br>'+
                     '							<form class="register-form">'+
                     '								<input id="login-email" type="email" class="form-control" placeholder="Email" maxlength="100" oninput="clearErrorLoginEmail()">'+
                     '								<small id="error-login-email" class="error-message"></small>'+
                     '								<br>'+
                     '								<input id="login-pass" type="password" class="form-control" placeholder="Password" maxlength="100" oninput="clearErrorLoginPass()">'+
                     '								<small id="error-login" class="error-message"></small>'+
                     '								<button id="login-button" class="btn btn-info btn-block btn-fill" onclick="login(event)" data-i18n-key="login-button-message">Intră în cont</button>'+
                     '								<!-- <h5>sau</h5> -->'+
                     '								<div id="reg-line"></div>'+
                     '<!--								<button id="login-fb-button" class="btn btn-facebook btn-block btn-fill" onclick="fbLogin(event)"><i class="fa fa-facebook" aria-hidden="true"> </i>Loghează-te cu Facebook</button>-->'+
                     '								<button id="login-google-button" class="btn btn-google btn-block btn-fill" onclick="googleButtonClick(event)"><i class="fa fa-google" aria-hidden="true"></i><span data-i18n-key="login-google">Loghează-te cu Google</span></button>'+
                     '								<div id="g_id_onload" data-client_id="801355334630-8i08j4vg23mh4b1eogrs5c0jrm5etb2n.apps.googleusercontent.com" data-callback="googleLogin" data-auto_prompt="false"></div>'+
                     '								<div class="g_id_signin btn-block" style="display: none" data-type="standard" data-size="large" data-theme="outline" data-text="sign_in_with" data-shape="rectangular" data-logo_alignment="center" data-locale="ro"></div>'+
                     '							</form>'+
                     '							<div class="forgot">'+
                     '								<a href="./login.html" class="btn btn-simple btn-info" data-i18n-key="login-account-benefits">Beneficiile unui cont de utilizator</a>'+
                     '								<a href="./forgot.html" class="btn btn-simple btn-info btn-reg" data-i18n-key="login-forgot-password">Am uitat parola</a>'+
                     '								<a class="btn btn-simple btn-info btn-reg" data-dismiss="modal" data-toggle="modal" data-target="#register-modal" data-i18n-key="login-send-to-register">Nu ai cont? Înregistrează-te</a>'+
                     '							</div>';

                     document.getElementById('login-card').innerHTML = loginModal;
}

function getRegisterModal() {
    var registerModal = '							<div class="close close-login" data-dismiss="modal">'+
                        '								<i class="fa fa-close"></i>'+
                        '							</div>'+
                        '							<h3 class="title" data-i18n-key="register-header">Creare cont nou</h3>'+
                        '							<br>'+
                        '							<form class="register-form">'+
                        '								<input id="register-email" type="email" class="form-control" placeholder="Email" maxlength="100" oninput="clearErrorRegisterEmail()">'+
                        '								<small id="error-register-email" class="error-message"></small>'+
                        '								<br>'+
                        '								<fieldset class="search-fieldset">'+
                        '									<input id="register-pass" type="password" class="form-control" placeholder="Password" maxlength="22" oninput="clearErrorRegisterPass()">'+
                        '									<i id="pass-info" data-toggle="tooltip" title="Parola trebuie să aibă minim 7 caractere și să conțină litere și cifre." class="fas fa-info-circle inner-icon" aria-hidden="true"></i>'+
                        '								</fieldset>'+
                        '								<input id="register-confirm-pass" type="password" class="form-control" placeholder="Confirm password" maxlength="22" oninput="clearErrorRegisterConfirmPass()">'+
                        '								<small id="error-register" class="error-message"></small>'+

                        '								<div>'+
                        '									<label class="checkbox" for="policy-check"><input type="checkbox" name="policy" value="" id="policy-check" data-toggle="checkbox"><span data-i18n-key="register-terms-prefix">Am citit și înțeles</span> <a href="./terms.html" target="_blank" rel="noopener" style="text-decoration: underline;" data-i18n-key="register-terms-link">Termenii și condițiile de utilizare</a> <span data-i18n-key="register-terms-conjunction">și</span> <a href="./policy.html" target="_blank" rel="noopener" style="text-decoration: underline;" data-i18n-key="register-policy-link">Politica de prelucrare a datelor cu caracter personal</a></label>'+
                        '									<label class="checkbox" for="newsletter-check"><input type="checkbox" name="news" value="" id="newsletter-check" data-toggle="checkbox"><span data-i18n-key="register-subscribe">Vreau să fiu la curent cu ultimele noutăți</span></label>'+
                        '								</div>'+

                        '								<button id="register-button" class="btn btn-info btn-block btn-fill" onclick="register(event)" data-i18n-key="register-button-message">Înregistrare</button>'+
                        '								<!-- <h5>sau</h5> -->'+
                        '								<div id="reg-line"></div>'+
                        '<!--								<button id="register-fb-button" class="btn btn-facebook btn-block btn-fill" onclick="fbLogin(event)"><i class="fa fa-facebook" aria-hidden="true"> </i>Loghează-te cu Facebook</button>-->'+
                        '								<button id="register-google-button" class="btn btn-google btn-block btn-fill" onclick="googleButtonClick(event)" data-i18n-key="login-google"><i class="fa fa-google" aria-hidden="true"> </i>Loghează-te cu Google</button>'+
                        '								<div class="g_id_signin btn-block" style="display: none" data-type="standard" data-size="large" data-theme="outline" data-text="sign_in_with" data-shape="rectangular" data-logo_alignment="center" data-locale="ro"></div>'+
                        '							</form>'+
                        '							<div class="forgot">'+
                        '								<a href="./register.html" class="btn btn-simple btn-info" data-i18n-key="login-account-benefits">Beneficiile unui cont de utilizator</a>'+
                        '							</div>';

                        document.getElementById('register-card').innerHTML = registerModal;

                        // ct-paper draws the visible checkbox as a decoration span the plugin
                        // inserts on init. Its DOM-ready pass already ran before this modal HTML
                        // was injected, so the two checkboxes (terms, newsletter) stay invisible
                        // until the first click lazily initializes them. Decorate them now.
                        if ($.fn.checkbox) {
                            $('#register-card [data-toggle="checkbox"]').checkbox();
                        }
}

function getPageHeader() {
	var headerString = '      <div class="container">\n' +
		'        <!-- Brand and toggle get grouped for better mobile display -->\n' +
		'        <div class="navbar-header">\n' +
		'          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#navigation-example-2">\n' +
		'            <span class="sr-only">Toggle navigation</span>\n' +
		'            <span class="icon-bar"></span>\n' +
		'            <span class="icon-bar"></span>\n' +
		'            <span class="icon-bar"></span>\n' +
		'          </button>\n' +
		'\t\t  <a class="navbar-brand" href="./">\n' +
		'\t\t\t<img class="main-logo" src="./assets/img/unistart_logo_huge.png">\n' +
		'\t\t  </a>\n' +
		'        </div>\n' +
		'    \n' +
		'        <!-- Collect the nav links, forms, and other content for toggling -->\n' +
		'        <div class="collapse navbar-collapse" id="navigation-example-2">\n' +
		'          <ul class="nav navbar-nav navbar-right">\n' +
		'            <li>\n' +
		'                <a href="./" class="btn btn-simple" data-i18n-key="header-footer-homepage">Acasă</a>\n' +
		'            </li>\n' +
		'            <li>\n' +
		'                <a href="./search.html" class="btn btn-simple" data-i18n-key="header-footer-faculties">Facultăți</a>\n' +
		'            </li>\n' +
		'            <li>\n' +
		'                <a href="./reviews.html" class="btn btn-simple" data-i18n-key="header-footer-reviews">Evaluări</a>\n' +
		'            </li>\n' +
		'			<li>\n' +
		'				<a href="./comparison.html" class="btn btn-simple" data-i18n-key="header-footer-compare">Compară</a>\n' +
		'			</li>\n' +
		'            <li>\n' +
		'                <a href="./contact.html" class="btn btn-simple" data-i18n-key="header-footer-contact">Contact</a>\n' +
		'            </li>\n' +
		'			<li>\n' +
		'                <a id="myaccount" class="btn myaccount-button" onclick="myAccount(this)"><i class="far fa-user" style="margin-right: 7px"></i><span data-i18n-key="header-footer-login">Contul meu</span></a>\n' +
		'            </li>\n' +
        '            <li style="margin-top: 7px; margin-left: 10px">\n' +
                        getLocaleFlags() +
        '            </li>\n' +
		'           </ul>\n' +
		'        </div><!-- /.navbar-collapse -->\n' +
		'      </div><!-- /.container-->';

	document.getElementById('demo-navbar').innerHTML = headerString;
}

function getPageFooter() {
	var footerString = '    <div class="container">\n' +
		'        <div class="row">\n' +
		'            <div class="col-md-6 col-sm-7">\n' +
		'                <div class="links">\n' +
		'                    <ul class="uppercase-links">\n' +
		'                        <li>\n' +
		'                            <a href="./" data-i18n-key="header-footer-homepage">\n' +
		'                                Acasă\n' +
		'                            </a>\n' +
		'                        </li>\n' +
		'                        <li>\n' +
		'                            <a href="./search.html" data-i18n-key="header-footer-faculties">\n' +
		'                                Facultăți\n' +
		'                            </a>\n' +
		'                        </li>\n' +
		'                        <li>\n' +
		'                            <a href="./reviews.html" data-i18n-key="header-footer-reviews">\n' +
		'                                Evaluări\n' +
		'                            </a>\n' +
		'                        </li>\n' +
		'                        <li>\n' +
		'                            <a href="./comparison.html" data-i18n-key="header-footer-compare">\n' +
		'                                Compară\n' +
		'                            </a>\n' +
		'                        </li>\n' +
		'                        <li>\n' +
		'                            <a href="./contact.html" data-i18n-key="header-footer-contact">\n' +
		'                               Contact\n' +
		'                            </a>\n' +
		'                        </li>\n' +
		'                    </ul>\n' +
		'					 <ul>\n' +
		'						<li>\n' +
		'                            <a href="./terms.html" data-i18n-key="header-footer-terms-and-conditions">\n' +
		'                               Termeni și condiții\n' +
		'                            </a>\n' +
		'                        </li>\n' +
		'						 <li>\n' +
		'                            <a href="./policy.html" data-i18n-key="header-footer-policy">\n' +
		'                               Politica de confidențialitate\n' +
		'                            </a>\n' +
		'                        </li>\n' +
		'						 <li>\n' +
		'                            <a href="#" onclick="openCookieSettings(event)" data-i18n-key="header-footer-cookie-settings">\n' +
		'                               Setări cookie-uri\n' +
		'                            </a>\n' +
		'                        </li>\n' +
		'					</ul>\n' +
		'                    <hr>\n' +
		'\n' +
		'                    <div class="copyright" data-i18n-key="copyright">\n' +
		'                        © 2020 Jamstudy, din dragoste pentru educaţie\n' +
		'                    </div>\n' +
		'                </div>\n' +
		'            </div>\n' +
		'\n' +
		'            <div class="col-md-4 col-md-offset-2 col-sm-4 col-sm-offset-1">\n' +
		'                <div class="social-area">\n' +
		'                        <a href="http://twitter.com" target="_blank"><button class="btn btn-icon btn-fill btn-twitter">\n' +
		'                            <i class="fa fa-twitter"></i>\n' +
		'                        </button>    \n' +
		'						</a>\n' +
		'                        <a href="https://www.facebook.com/UnistartFb" target="_blank"><button class="btn btn-icon btn-fill btn-facebook">\n' +
		'                            <i class="fa fa-facebook"> </i>\n' +
		'                        </button>\n' +
		'						</a>\n' +
		'						<a href="https://www.instagram.com/unistart.ro" target="_blank"><button class="btn btn-icon btn-fill btn-instagram">\n' +
		'                            <i class="fa fa-instagram"></i>\n' +
		'                        </button>\n' +
		'						</a>\n' +
		'                        <a href="http://youtube.com" target="_blank"><button class="btn btn-icon btn-fill btn-youtube">\n' +
		'                            <i class="fa fa-youtube"></i>\n' +
		'                        </button>\n' +
		'						</a>\n' +
		'                </div>\n' +
		'            </div>\n' +
		'        </div>\n' +
		'\n' +
		'    </div>';

	document.getElementById('footer').innerHTML = footerString;
}

function getUField(key) {
	var u = localStorage.getItem("u");
	if (u) {
		var uJson = JSON.parse(u);
		return uJson[key];
	}
}

function setUField(key, object) {
	var u = localStorage.getItem("u");
	if (u) {
		var uJson = JSON.parse(u);
		uJson[key] = object;
	}
	localStorage.setItem("u", JSON.stringify(uJson));
}

// Entity Admin portal
function getAField(key) {
	var a = localStorage.getItem("a");
	if (a) {
		var aJson = JSON.parse(a);
		return aJson[key];
	}
}

function setAField(key, object) {
	var a = localStorage.getItem("a");
	if (a) {
		var aJson = JSON.parse(a);
		aJson[key] = object;
	}
	localStorage.setItem("a", JSON.stringify(aJson));
}



// Languages

const ro_json = {
                  "search-v1": "Caută",
                  "search-v2": "Căutare",
                  "sort": "Sortare",
                  "add-review": "Adaugă evaluare",
                  "add-review-tooltip": "Adaugă evaluare 100% anonim",
                  "qa-tab": "Q&A",
                  "qa-sort-newest": "Cea mai recentă",
                  "qa-sort-oldest": "Cea mai veche",
                  "qa-sort-most-voted": "Cea mai votată",
                  "qa-ask-question": "Pune o întrebare",
                  "qa-answer": "Răspunde",
                  "qa-no-questions": "Nu au fost găsite întrebări",
                  "qa-show-more-answers-prefix": "Vezi încă",
                  "qa-show-more-answers-suffix": "răspunsuri",
                  "see-all": "+ Vezi tot",
                  "see-all-v2": "Vezi tot",
                  "see-less": "- Vezi mai puțin",
                  "save": "Salvează",
                  "saved": "Salvată",
                  "compare": "Compară",
                  "see-comparison": "Vezi Comparaţii",
                  "compare-added-tooltip": "Facultatea a fost adăugată la comparații. Apasă din nou pentru a le vedea.",
                  "compare-with-other-faculties": "Compară cu alte facultăți",
                  "see-description": "Vezi descriere",
                  "review-v1": "Evaluare",
                  "review-v2": "evaluare",
                  "reviews-v1": "Evaluări",
                  "reviews-v2": "evaluări",
                  "rating": "Notă",
                  "see-all-faculties": "Vezi toate facultățile",
                  "see-all-reviews": "Vezi toate evaluările",
                  "header-footer-homepage": "Acasă",
                  "header-footer-faculties": "Facultăți",
                  "header-footer-reviews": "Evaluări",
                  "header-footer-compare": "Compară",
                  "header-footer-contact": "Contact",
                  "header-footer-login": "Autentificare",
                  "header-footer-my-account": "Contul meu",
                  "header-footer-terms-and-conditions": "Termeni și condiții",
                  "header-footer-policy": "Politica de confidențialitate",
                  "header-footer-cookie-settings": "Setări cookie-uri",
                  "copyright": "© 2020 Jamstudy, din dragoste pentru educaţie",
                  "load-more": "Încarcă mai multe rezultate",
                  "recently-viewed": "Recent vizualizate",
                  "study-domains": "Domenii de studiu",
                  "cities": "Orașe",
                  "license": "Licență",
                  "master": "Masterat",
                  "leave-feedback": "Lasă-ne feedback",
                  "email": "Email",
                  "password": "Parolă",
                  "from": "din",
                  "yes": "Da",
                  "no": "Nu",
                  "not-applicable": "Nu se aplică",
                  "student": "Student",
                  "master-student": "Masterand",
                  "alumni": "Absolvent",
                  "retired": "M-am retras",
                  "other": "Altul",
                  "the-year": "Anul",
                  "app-title": "Unistart - Evaluări facultăți",
                  "title": "Alege facultatea potrivită pentru tine",
                  "search-keywords": "Caută după cuvinte cheie",
                  "search-keywords-placeholder": "Facultate, Universitate, Oraș ....",
                  "search-by-domain-or-city": "Sau vezi ce facultate ți se potrivește după domeniul de studiu si oraș",
                  "domain-economy": "Științe Economice",
                  "domain-engineering": "Științe Inginerești",
                  "domain-politics": "Științe Politice și Comunicare",
                  "domain-humanities": "Științe Umaniste",
                  "domain-judicial": "Științe Juridice",
                  "domain-medicine": "Medicină",
                  "domain-nature": "Științe ale Naturii",
                  "domain-art-v1": "Arte Arhitectură și Urbanism",
                  "domain-art-v2": "Arte, Arhitectură și Urbanism",
                  "domain-exact": "Științe Exacte",
                  "domain-social": "Științe Sociale și Psihologie",
                  "domain-agriculture": "Științe Agricole",
                  "domain-sports": "Ştiința Sportului şi Educației Fizice",
                  "domain-military-v1": "Ştiinţe Militare Informaţii şi Ordine publică",
                  "domain-military-v2": "Științe Militare şi de Informații",
                  "domain-theology": "Teologie",
                  "top-faculties-title": "Top Facultăți conform studenților",
                  "top-faculties-subtitle": "Top-ul este alcătuit pe baza notelor acordate de studenți facultăților pe platforma UNISTART",
                  "all-domains": "Toate domeniile",
                  "all-cities": "Toate oraşele",
                  "first-place": "Primul Loc",
                  "newsletter-title": "Te ajutăm să iei deciziile bune pentru educația ta!",
                  "newsletter-subtitle": "Abonează-te la newsletter-ul nostru și primești săptămânal știri utile și informări din domeniul educației!",
                  "newsletter-subscribe-me": "Abonează-mă",
                  "sort-faculty-name-asc": "Facultate ascendent",
                  "sort-faculty-name-desc": "Facultate descendent",
                  "sort-university-name-asc": "Universitate ascendent",
                  "sort-university-name-desc": "Universitate descendent",
                  "sort-city-asc": "Oraș ascendent",
                  "sort-city-desc": "Oraș descendent",
                  "sort-rating-asc": "Notă ascendent",
                  "sort-rating-desc": "Notă descendent",
                  "access-website": "Accesează website",
                  "profile": "Profil",
                  "would-recommend-faculty": "Ar recomanda facultatea",
                  "would-recommend": "Ar recomanda",
                  "would-not-recommend": "Nu ar recomanda",
                  "difficulty": "Dificultate",
                  "review-professors-and-courses": "Profesori și cursuri",
                  "review-career-opportunities": "Oportunități în carieră",
                  "review-acommodation": "Cazare",
                  "review-student-associations": "Asociații studențești",
                  "review-facilities": "Facilități",
                  "review-study-time-vs-free-time": "Timp de studiu vs. timp liber",
                  "review-5-stars": "5 stele",
                  "review-4-stars": "4 stele",
                  "review-3-stars": "3 stele",
                  "review-2-stars": "2 stele",
                  "review-1-stars": "1 stea",
                  "faculty-official-presentation": "Prezentare oficială",
                  "faculty-general-presentation": "Prezentare generală",
                  "faculty-general-presentation-website": "Website",
                  "faculty-general-presentation-address": "Adresă",
                  "faculty-general-presentation-phone": "Telefon",
                  "faculty-general-presentation-description": "Descriere",
                  "faculty-general-presentation-duration": "Durată",
                  "faculty-general-presentation-license-places": "Locuri licență",
                  "faculty-general-presentation-master-places": "Locuri masterat",
                  "faculty-general-presentation-license-budget-places": "Locuri buget licență",
                  "faculty-general-presentation-master-budget-places": "Locuri buget masterat",
                  "faculty-general-presentation-license-tax-places": "Locuri taxă  licență",
                  "faculty-general-presentation-master-tax-places": "Locuri taxă  licență",
                  "faculty-general-presentation-license-active-students": "Studenți activi licență",
                  "faculty-general-presentation-master-active-students": "Studenți activi masterat",
                  "faculty-general-presentation-number-of-professors": "Număr profesori",
                  "faculty-general-presentation-students-to-professors-ratio": "Nr. studenți raportat la profesori",
                  "faculty-general-presentation-group-contact": "Contact",
                  "faculty-general-presentation-group-about": "Despre",
                  "faculty-general-presentation-group-places": "Locuri disponibile",
                  "faculty-general-presentation-group-stats": "Statistici",
                  "faculty-career-opportunities": "Oportunități în carieră",
                  "other-faculties-same-university": "Alte facultăți de la aceeași universitate",
                  "study-programs": "Programe de studiu",
                  "study-programs-domain": "Domeniu",
                  "study-programs-specialization": "Specializare",
                  "study-programs-admission": "Admitere",
                  "study-programs-accreditation": "Acreditare",
                  "study-programs-available-places": "Locuri disponibile",
                  "study-programs-budget-places": "Locuri buget",
                  "study-programs-tax-places": "Locuri taxă",
                  "study-programs-last-grade": "Ultima notă",
                  "study-programs-candidates-per-place": "Candidați pe loc",
                  "study-programs-annual-tax": "Taxa anuală",
                  "no-reviews": "Nu există evaluări",
                  "no-more-reviews": "Nu mai există alte evaluări pentru această facultate",
                  "compare-faculties": "Compară facultăți",
                  "compare-suggestions": "Sugestii de comparație",
                  "compare-add-faculty": "Adaugă o facultate pentru a începe",
                  "compare-search-faculties": "Caută Facultăți",
                  "compare-search-placeholder": "Caută o facultate pentru a o adăuga în tabel...",
                  "compare-search-no-results": "Nu au fost găsite rezultate",
                  "compare-radar-title": "Comparație vizuală",
                  "compare-table-title": "Tabel comparativ",
                  "compare-radar-max-faculties": "Notă: graficul arată doar primele 5 facultăți (maxim) din tabel",
                  "featured-review-label": "Ce spun studenții",
                  "review-filter-user-status": "Filtrează după tipul autorului",
                  "expand-all-reviews": "+ Extinde toate evaluările",
                  "collapse-all-reviews": "- Restrânge toate evaluările",
                  "contact-us": "Contactează-ne",
                  "contact-subtitle": "Spune-ne ce părere ai despre platforma Unistart, raportează un bug sau, pur și simplu, lasă-ne un mesaj.",
                  "contact-name": "Nume",
                  "contact-message": "Mesaj",
                  "contact-send": "Trimite",
                  "login-header": "Autentificare",
                  "login-button-message": "Intră în cont",
                  "login-google": "Loghează-te cu Google",
                  "login-account-benefits": "Beneficiile unui cont de utilizator",
                  "login-forgot-password": "Am uitat parola",
                  "login-send-to-register": "Nu ai cont? Înregistrează-te",
                  "register-header": "Creare cont nou",
                  "register-confirm-password": "Confirmă Parola",
                  "register-terms-and-conditions": "Am citit și înțeles Termenii și condițiile de utilizare și Politica de prelucrare a datelor cu caracter personal",
                  "register-terms-prefix": "Am citit și înțeles",
                  "register-terms-link": "Termenii și condițiile de utilizare",
                  "register-terms-conjunction": "și",
                  "register-policy-link": "Politica de prelucrare a datelor cu caracter personal",
                  "register-subscribe": "Vreau să fiu la curent cu ultimele noutăți",
                  "register-button-message": "Înregistrare",
                  "register-send-to-login": "Ai deja cont? Autentifică-te",
                  "benefits-subtitle": "Fa-ti cont pe Unistart pentru a putea avea access la toate sectiunile platformei.",
                  "benefits-1": "Evaluează facultăți.",
                  "benefits-2": "Salvează facultățile favorite.",
                  "benefits-3": "(În curând) Postează intrebări.",
                  "benefits-4": "Personalizează-ți contul.",
                  "benefits-5": "Votează evaluări.",
                  "benefits-6": "(În curând) Optează pentru a primi notificări pentru facultățile favorite.",
                  "benefits-7": "Vei avea acces la viitoarele functionalități dedicate exclusiv utilizatorilor înregistrați.",
                  "add-review-general-review-title": "Evaluare generală",
                  "add-review-give-rating": "Acordă o notă",
                  "add-review-very-unsatisfied": "Foarte nemulțumit",
                  "add-review-unsatisfied": "Nemulțumit",
                  "add-review-satisfied": "Mulțumit",
                  "add-review-very-satisfied": "Foarte mulțumit",
                  "add-review-remaining-characters": " caractere obligatorii rămase",
                  "add-review-next-step": "Pasul următor",
                  "add-review-previous-step": "Pasul anterior",
                  "add-review-the-step": "Pasul",
                  "add-review-engage-text-title": "Evaluează experienţa la această facultate, 100% anonim",
                  "add-review-engage-text-1": "Adu-ți aminte cât de mult te-ai gândit ce facultate să urmezi și cât de mult ți-ai fi dorit să ştii cum se desfașoară orele, cât de pregatiți sunt profesorii, ce oportunități de job-uri vei avea, situația asociațiilor studențești sau viața de cămin. ",
                  "add-review-engage-text-2": "Acum ai ocazia să adaugi o evaluare pentru facultatea aleasă si să ajuți cu informații un elev care dorește să o ia pe același drum. Evaluarea ta poate face diferența. Durează doar un minut.",
                  "add-review-categories-title": "Evaluare pe categorii. Această pagină este opțională.",
                  "add-review-would-you-recommend": "Ai recomanda facultatea?",
                  "add-review-something-about-yourself": "Spune-ne câte ceva despre tine",
                  "add-review-manage-to-get-hired": "Ai reușit să te angajezi, in domeniu, in timpul facultății sau la cel mult 6 luni dupa terminarea ei?",
                  "add-review-post": "Postează evaluarea",
                  "add-review-agree-to-terms-and-policy-1": "Prin adăugarea acestei evaluări declar că sunt de acord cu",
                  "add-review-agree-to-terms-and-policy-2": "ale platformei.",
                  "add-review-agree-to-terms-and-policy-3": "și cu",
                  "add-review-agree-to-terms-and-policy-4": " Termenii și Condițiile ",
                  "add-review-agree-to-terms-and-policy-5": " Politica de Confidențialitate ",
                  "add-question-agree-to-terms-and-policy-1": "Prin adăugarea acestei întrebări declar că sunt de acord cu",
                  "qa-ask-question-title-label": "Titlu",
                  "qa-ask-question-text-label": "Întrebare",
                  "qa-ask-question-title-placeholder": "Scrie titlul întrebării tale aici. Minim 5, maxim 150 de caractere.",
                  "qa-ask-question-text-placeholder": "Scrie întrebarea ta aici. Minim 10, maxim 1000 de caractere.",
                  "qa-post-question": "Postează întrebarea",
                  "qa-post-answer": "Postează răspunsul",
                  "add-review-success-message": "Evaluarea a fost adăugată cu succes. O poți edita sau șterge în primele 48 de ore, din contul tău.",
                  "add-review-general-review-placeholder": "Descrie experiența ta în cel puțin 30 de caractere...",
                  "add-review-professors-and-courses-placeholder": "Cum sunt profesorii? Cum se desfasoară orele? Cum sunt materialele de studiu?",
                  "add-review-career-opportunities-placeholder": "Fie că ai terminat facultatea sau ești încă student, descrie experiența ta legată de oportunitățile de angajare...",
                  "add-review-acommodation-placeholder": "Locuiești în cămin sau în chirie? Descrie locuința, prețul, condițiile...",
                  "add-review-student-associations-placeholder": "Cât de reprezentat te simți de asociațiile studențesti? Ce fel de activități și evenimente organizează?",
                  "add-review-facilities-placeholder": "Descrie facilitățile si starea lor (biblioteca, aula, laboratoarele, echipamentele).",
                  "add-review-study-time-vs-free-time-placeholder": "Câte ore petreci pe săptamână la facultate? Câte ore studiezi sau lucrezi la proiecte?",
                  "user-profile-title-v1": "Profilul tău este incomplet!",
                  "user-profile-title-v2": "Profilul tău este complet!",
                  "user-profile-subtitle-v1": "Ca să iți putem oferi o experiență personalizată, te rugăm să completezi profilul.",
                  "user-profile-subtitle-v2": "Ne bucurăm să îți putem oferi o experiență personalizată.",
                  "user-profile-profile-text": "Profil și informații",
                  "user-profile-last-name": "Nume",
                  "user-profile-first-name": "Prenume",
                  "user-profile-reset-password": "Resetează parola",
                  "user-profile-city": "Localitate",
                  "user-profile-birthday": "Data nașterii",
                  "user-profile-domains-text": "În ce domenii ai fi interesat să studiezi?",
                  "user-profile-cities-text": "În ce orașe ai fi interesat să studiezi?",
                  "user-profile-subscribe-text": "Abonare la newsletter",
                  "user-profile-saved-faculties": "Facultăți salvate",
                  "user-profile-saved-faculties-empty": "Nu ai salvat nicio facultate",
                  "user-profile-notify-text": "Notifică-mă când apar evaluări noi",
                  "user-profile-my-reviews": "Evaluările mele",
                  "user-profile-my-reviews-empty": "Nu ai adăugat nicio evaluare",
                  "user-profile-saved-companies": "Companii salvate",
                  "user-profile-saved-companies-empty": "Nu ai salvat nicio companie",
                  "user-profile-sign-out": "Ieși din cont"
                };

const en_json = {
                  "search-v1": "Search",
                  "search-v2": "Search",
                  "sort": "Sort",
                  "add-review": "Add Review",
                  "add-review-tooltip": "Add 100% anonymous review",
                  "qa-tab": "Q&A",
                  "qa-sort-newest": "Newest",
                  "qa-sort-oldest": "Oldest",
                  "qa-sort-most-voted": "Most voted",
                  "qa-ask-question": "Ask a question",
                  "qa-answer": "Answer",
                  "qa-no-questions": "No questions were found",
                  "qa-show-more-answers-prefix": "Show",
                  "qa-show-more-answers-suffix": "more answers",
                  "see-all": "+ See all",
                  "see-all-v2": "See all",
                  "see-less": "- See less",
                  "save": "Save",
                  "saved": "Saved",
                  "compare": "Compare",
                  "see-comparison": "See Comparisons",
                  "compare-added-tooltip": "Faculty added to comparisons. Click again to view them.",
                  "compare-with-other-faculties": "Compare with other faculties",
                  "see-description": "See description",
                  "review-v1": "Review",
                  "review-v2": "review",
                  "reviews-v1": "Reviews",
                  "reviews-v2": "reviews",
                  "rating": "Rating",
                  "see-all-faculties": "See all faculties",
                  "see-all-reviews": "See all reviews",
                  "header-footer-homepage": "Home",
                  "header-footer-faculties": "Faculties",
                  "header-footer-reviews": "Reviews",
                  "header-footer-compare": "Compare",
                  "header-footer-contact": "Contact",
                  "header-footer-login": "Login",
                  "header-footer-my-account": "My Account",
                  "header-footer-terms-and-conditions": "Terms and Conditions",
                  "header-footer-policy": "Privacy Policy",
                  "header-footer-cookie-settings": "Cookie Settings",
                  "copyright": "© 2020 Jamstudy, out of love for education",
                  "load-more": "Load more results",
                  "recently-viewed": "Recently viewed",
                  "study-domains": "Study domains",
                  "cities": "Cities",
                  "license": "License",
                  "master": "Master's",
                  "leave-feedback": "Leave feedback",
                  "email": "Email",
                  "password": "Password",
                  "from": "from",
                  "yes": "Yes",
                  "no": "No",
                  "not-applicable": "N/A",
                  "student": "Student",
                  "master-student": "MA",
                  "alumni": "Alumni",
                  "retired": "I dropped out",
                  "other": "Other",
                  "the-year": "Year",
                  "app-title": "Unistart - Faculty Reviews",
                  "title": "Choose the right faculty for you",
                  "search-keywords": "Search by keywords",
                  "search-keywords-placeholder": "Faculty, University, City ....",
                  "search-by-domain-or-city": "Or see which faculty suits you based on the study domain and city",
                  "domain-economy": "Economic Sciences",
                  "domain-engineering": "Engineering Sciences",
                  "domain-politics": "Political Science and Communication",
                  "domain-humanities": "Humanities",
                  "domain-judicial": "Juridical Sciences",
                  "domain-medicine": "Medicine",
                  "domain-nature": "Natural Sciences",
                  "domain-art-v1": "Arts, Architecture, and Urbanism",
                  "domain-art-v2": "Arts, Architecture, and Urbanism",
                  "domain-exact": "Exact Sciences",
                  "domain-social": "Social Sciences and Psychology",
                  "domain-agriculture": "Agricultural Sciences",
                  "domain-sports": "Sports Science and Physical Education",
                  "domain-military-v1": "Military Sciences, Information, and Public Order",
                  "domain-military-v2": "Military and Information Sciences",
                  "domain-theology": "Theology",
                  "top-faculties-title": "Top Faculties according to students",
                  "top-faculties-subtitle": "The top is based on the ratings given by students to faculties on the UNISTART platform",
                  "all-domains": "All domains",
                  "all-cities": "All cities",
                  "first-place": "First place",
                  "newsletter-title": "We help you make the right decisions for your education!",
                  "newsletter-subtitle": "Subscribe to our newsletter and receive useful weekly news and updates from the education field!",
                  "newsletter-subscribe-me": "Subscribe me",
                  "sort-faculty-name-asc": "Faculty ascending",
                  "sort-faculty-name-desc": "Faculty descending",
                  "sort-university-name-asc": "University ascending",
                  "sort-university-name-desc": "University descending",
                  "sort-city-asc": "City ascending",
                  "sort-city-desc": "City descending",
                  "sort-rating-asc": "Rating ascending",
                  "sort-rating-desc": "Rating descending",
                  "access-website": "Access website",
                  "profile": "Profile",
                  "would-recommend-faculty": "Would recommend the faculty",
                  "would-recommend": "Would recommend",
                  "would-not-recommend": "Would not recommend",
                  "difficulty": "Difficulty",
                  "review-professors-and-courses": "Professors and courses",
                  "review-career-opportunities": "Career opportunities",
                  "review-acommodation": "Accommodation",
                  "review-student-associations": "Student associations",
                  "review-facilities": "Facilities",
                  "review-study-time-vs-free-time": "Study time vs. free time",
                  "review-5-stars": "5 stars",
                  "review-4-stars": "4 stars",
                  "review-3-stars": "3 stars",
                  "review-2-stars": "2 stars",
                  "review-1-stars": "1 star",
                  "faculty-official-presentation": "Official presentation",
                  "faculty-general-presentation": "General presentation",
                  "faculty-general-presentation-website": "Website",
                  "faculty-general-presentation-address": "Address",
                  "faculty-general-presentation-phone": "Phone",
                  "faculty-general-presentation-description": "Description",
                  "faculty-general-presentation-duration": "Duration",
                  "faculty-general-presentation-license-places": "License places",
                  "faculty-general-presentation-master-places": "Master's places",
                  "faculty-general-presentation-license-budget-places": "Budget license places",
                  "faculty-general-presentation-master-budget-places": "Budget master's places",
                  "faculty-general-presentation-license-tax-places": "Tax license places",
                  "faculty-general-presentation-master-tax-places": "Tax master's places",
                  "faculty-general-presentation-license-active-students": "Active license students",
                  "faculty-general-presentation-master-active-students": "Active master's students",
                  "faculty-general-presentation-number-of-professors": "Number of professors",
                  "faculty-general-presentation-students-to-professors-ratio": "Student to professor ratio",
                  "faculty-general-presentation-group-contact": "Contact",
                  "faculty-general-presentation-group-about": "About",
                  "faculty-general-presentation-group-places": "Available places",
                  "faculty-general-presentation-group-stats": "Statistics",
                  "faculty-career-opportunities": "Career opportunities",
                  "other-faculties-same-university": "Other faculties from the same university",
                  "study-programs": "Study programs",
                  "study-programs-domain": "Domain",
                  "study-programs-specialization": "Specialization",
                  "study-programs-admission": "Admission",
                  "study-programs-accreditation": "Accreditation",
                  "study-programs-available-places": "Available places",
                  "study-programs-budget-places": "Budget places",
                  "study-programs-tax-places": "Tax places",
                  "study-programs-last-grade": "Last grade",
                  "study-programs-candidates-per-place": "Candidates per place",
                  "study-programs-annual-tax": "Annual tax",
                  "no-reviews": "There are no reviews",
                  "no-more-reviews": "There are no more reviews for this faculty",
                  "compare-faculties": "Compare faculties",
                  "compare-suggestions": "Comparison suggestions",
                  "compare-add-faculty": "Add a faculty to start",
                  "compare-search-faculties": "Search for Faculties",
                  "compare-search-placeholder": "Search for a faculty to add it to the table...",
                  "compare-search-no-results": "No results were found",
                  "compare-radar-title": "Visual comparison",
                  "compare-table-title": "Comparison table",
                  "compare-radar-max-faculties": "Note: the chart shows only the first 5 faculties (max) from the table",
                  "featured-review-label": "What students say",
                  "review-filter-user-status": "Filter by author type",
                  "expand-all-reviews": "+ Expand all reviews",
                  "collapse-all-reviews": "- Collapse all reviews",
                  "contact-us": "Contact us",
                  "contact-subtitle": "Tell us what you think about the Unistart platform, report a bug, or simply leave us a message.",
                  "contact-name": "Name",
                  "contact-message": "Message",
                  "contact-send": "Send",
                  "login-header": "Login",
                  "login-button-message": "Sign in to your account",
                  "login-google": "Sign in with Google",
                  "login-account-benefits": "Benefits of a user account",
                  "login-forgot-password": "Forgot password",
                  "login-send-to-register": "Don't have an account? Register",
                  "register-header": "Create new account",
                  "register-confirm-password": "Confirm Password",
                  "register-terms-and-conditions": "I have read and understood the Terms and Conditions of use and the Privacy Policy",
                  "register-terms-prefix": "I have read and understood the",
                  "register-terms-link": "Terms and Conditions of use",
                  "register-terms-conjunction": "and the",
                  "register-policy-link": "Privacy Policy",
                  "register-subscribe": "I want to be informed of the latest news",
                  "register-button-message": "Register",
                  "register-send-to-login": "Already have an account? Log in",
                  "benefits-subtitle": "Create an account on Unistart to gain access to all platform sections.",
                  "benefits-1": "Rate faculties.",
                  "benefits-2": "Save favorite faculties.",
                  "benefits-3": "(Coming soon) Ask questions.",
                  "benefits-4": "Personalize your account.",
                  "benefits-5": "Vote on reviews.",
                  "benefits-6": "(Coming soon) Opt to receive notifications for favorite faculties.",
                  "benefits-7": "You will have access to future features exclusive to registered users.",
                  "add-review-general-review-title": "General review",
                  "add-review-give-rating": "Add rating",
                  "add-review-very-unsatisfied": "Very unsatisfied",
                  "add-review-unsatisfied": "Unsatisfied",
                  "add-review-satisfied": "Satisfied",
                  "add-review-very-satisfied": "Very satisfied",
                  "add-review-remaining-characters": " characters remaining",
                  "add-review-next-step": "Next step",
                  "add-review-previous-step": "Previous step",
                  "add-review-the-step": "Step",
                  "add-review-engage-text-title": "Evaluate your experience at this faculty, 100% anonymous",
                  "add-review-engage-text-1": "Remember how much you thought about which faculty to attend and how much you would have liked to know how the classes go, how prepared the professors are, what job opportunities you will have, the situation of student associations, or dorm life.",
                  "add-review-engage-text-2": "Now you have the chance to add a review for the chosen faculty and help a student who wants to follow the same path. Your review can make a difference. It only takes a minute.",
                  "add-review-categories-title": "Review by categories. This page is optional.",
                  "add-review-would-you-recommend": "Would you recommend the faculty?",
                  "add-review-something-about-yourself": "Tell us something about yourself",
                  "add-review-manage-to-get-hired": "Did you manage to get a job in your field during your studies or within 6 months after graduation?",
                  "add-review-post": "Post the review",
                  "add-review-agree-to-terms-and-policy-1": "By adding this review, I agree to the",
                  "add-review-agree-to-terms-and-policy-2": "of the platform.",
                  "add-review-agree-to-terms-and-policy-3": "and the",
                  "add-review-agree-to-terms-and-policy-4": " Terms and Conditions ",
                  "add-review-agree-to-terms-and-policy-5": " Privacy Policy ",
                  "add-question-agree-to-terms-and-policy-1": "By adding this question, I agree to the",
                  "qa-ask-question-title-label": "Title",
                  "qa-ask-question-text-label": "Question",
                  "qa-ask-question-title-placeholder": "Write your question's title here. Minimum 5, maximum 150 characters.",
                  "qa-ask-question-text-placeholder": "Write your question here. Minimum 10, maximum 1000 characters.",
                  "qa-post-question": "Post the question",
                  "qa-post-answer": "Post the answer",
                  "add-review-success-message": "Your review was added successfully. You can edit or delete it within 48 hours from your account.",
                  "add-review-general-review-placeholder": "Describe your experience in at least 30 characters...",
                  "add-review-professors-and-courses-placeholder": "What are the professors like? How are the classes conducted? What are the study materials like?",
                  "add-review-career-opportunities-placeholder": "Whether you have graduated or are still a student, describe your experience with employment opportunities...",
                  "add-review-acommodation-placeholder": "Do you live in a dormitory or rent? Describe the accommodation, price, conditions...",
                  "add-review-student-associations-placeholder": "How well represented do you feel by the student associations? What kind of activities and events do they organize?",
                  "add-review-facilities-placeholder": "Describe the facilities and their condition (library, auditorium, laboratories, equipment).",
                  "add-review-study-time-vs-free-time-placeholder": "How many hours do you spend per week at university? How many hours do you study or work on projects?",
                  "user-profile-title-v1": "Your profile is incomplete!",
                  "user-profile-title-v2": "Your profile is complete!",
                  "user-profile-subtitle-v1": "In order to provide you with a personalized experience, please complete your profile.",
                  "user-profile-subtitle-v2": "We are happy to offer you a personalized experience.",
                  "user-profile-profile-text": "Profile and Information",
                  "user-profile-last-name": "Last name",
                  "user-profile-first-name": "First name",
                  "user-profile-reset-password": "Reset password",
                  "user-profile-city": "City",
                  "user-profile-birthday": "Birthday",
                  "user-profile-domains-text": "In which fields would you be interested in studying?",
                  "user-profile-cities-text": "In which cities would you be interested in studying?",
                  "user-profile-subscribe-text": "Subscribe to newsletter",
                  "user-profile-saved-faculties": "Saved faculties",
                  "user-profile-saved-faculties-empty": "You have not saved any faculties",
                  "user-profile-notify-text": "Notify me when new reviews are available",
                  "user-profile-my-reviews": "My reviews",
                  "user-profile-my-reviews-empty": "You have not added any reviews",
                  "user-profile-saved-companies": "Saved companies",
                  "user-profile-saved-companies-empty": "You have not saved any companies",
                  "user-profile-sign-out": "Sign out"
                };

const lt_json = {
                  "search-v1": "Ieškoti",
                  "search-v2": "Ieškoti",
                  "sort": "Rūšiavimas",
                  "add-review": "Pridėti atsiliepimą",
                  "add-review-tooltip": "Pridėkite 100% anonimišką atsiliepimą",
                  "qa-tab": "Q&A",
                  "qa-sort-newest": "Naujausi",
                  "qa-sort-oldest": "Seniausi",
                  "qa-sort-most-voted": "Daugiausiai balsuoti",
                  "qa-ask-question": "Užduokite klausimą",
                  "qa-answer": "Atsakyti",
                  "qa-no-questions": "Klausimų nerasta",
                  "qa-show-more-answers-prefix": "Peržiūrėti dar",
                  "qa-show-more-answers-suffix": "atsakymus",
                  "see-all": "+ Peržiūrėti viską",
                  "see-all-v2": "Peržiūrėti viską",
                  "see-less": "- Mažiau matyti",
                  "save": "Išsaugoti",
                  "saved": "Išsaugota",
                  "compare": "Palyginti",
                  "see-comparison": "Peržiūrėti palyginimus",
                  "compare-added-tooltip": "Fakultetas pridėtas prie palyginimų. Spustelėkite dar kartą, kad juos pamatytumėte.",
                  "compare-with-other-faculties": "Palygink su kitais fakultetais",
                  "see-description": "Žr. aprašymą",
                  "review-v1": "Atsiliepimas",
                  "review-v2": "atsiliepimas",
                  "reviews-v1": "Atsiliepimai",
                  "reviews-v2": "atsiliepimai",
                  "rating": "Įvertinimas",
                  "see-all-faculties": "Peržiūrėti visas fakultetus",
                  "see-all-reviews": "Peržiūrėti visus atsiliepimus",
                  "header-footer-homepage": "Pagrindinis puslapis",
                  "header-footer-faculties": "Fakultetai",
                  "header-footer-reviews": "Atsiliepimai",
                  "header-footer-compare": "Palyginti",
                  "header-footer-contact": "Kontaktai",
                  "header-footer-login": "Prisijungti",
                  "header-footer-my-account": "Mano paskyra",
                  "header-footer-terms-and-conditions": "Naudojimosi sąlygos",
                  "header-footer-policy": "Privatumo politika",
                  "header-footer-cookie-settings": "Slapukų nustatymai",
                  "copyright": "© 2020 Jamstudy, su meile švietimui",
                  "load-more": "Įkelti daugiau rezultatų",
                  "recently-viewed": "Neseniai peržiūrėti",
                  "study-domains": "Studijų sritys",
                  "cities": "Miestai",
                  "license": "Licencija",
                  "master": "Magistras",
                  "leave-feedback": "Palikite atsiliepimą",
                  "email": "El. paštas",
                  "password": "Slaptažodis",
                  "from": "iš",
                  "yes": "Taip",
                  "no": "Ne",
                  "not-applicable": "Netaikoma",
                  "student": "Studentas",
                  "master-student": "Magistrantas",
                  "alumni": "Alumnas",
                  "retired": "Išėjau",
                  "other": "Kitas",
                  "the-year": "Metai",
                  "app-title": "Unistart - Fakultetų atsiliepimai",
                  "title": "Pasirinkite tinkamą fakultetą",
                  "search-keywords": "Ieškoti pagal raktažodžius",
                  "search-keywords-placeholder": "Fakultetas, Universitetas, Miestas ....",
                  "search-by-domain-or-city": "Arba peržiūrėkite, koks fakultetas jums tinka pagal studijų sritį ir miestą",
                  "domain-economy": "Ekonomikos mokslai",
                  "domain-engineering": "Inžinerijos mokslai",
                  "domain-politics": "Politikos ir komunikacijos mokslai",
                  "domain-humanities": "Humanitariniai mokslai",
                  "domain-judicial": "Teisės mokslai",
                  "domain-medicine": "Medicina",
                  "domain-nature": "Gamtos mokslai",
                  "domain-art-v1": "Menas Architektūra ir Urbanistika",
                  "domain-art-v2": "Menas, Architektūra ir Urbanistika",
                  "domain-exact": "Tiksliuosius mokslus",
                  "domain-social": "Socialiniai mokslai ir psichologija",
                  "domain-agriculture": "Žemės ūkio mokslai",
                  "domain-sports": "Sporto ir kūno kultūros mokslai",
                  "domain-military-v1": "Kariniai mokslai, informacija ir viešoji tvarka",
                  "domain-military-v2": "Kariniai mokslai ir žvalgyba",
                  "domain-theology": "Teologija",
                  "top-faculties-title": "Top Fakultetai pagal studentus",
                  "top-faculties-subtitle": "Top sąrašas sudarytas pagal studentų įvertinimus fakultetams UNISTART platformoje",
                  "all-domains": "Visos sritys",
                  "all-cities": "Visi miestai",
                  "first-place": "Pirmoji vieta",
                  "newsletter-title": "Padedame priimti teisingus sprendimus jūsų švietimui!",
                  "newsletter-subtitle": "Prenumeruokite mūsų naujienlaiškį ir kiekvieną savaitę gaukite naudingas naujienas ir informaciją apie švietimą!",
                  "newsletter-subscribe-me": "Prenumeruoti",
                  "sort-faculty-name-asc": "Fakultetas pagal pavadinimą didėjimo tvarka",
                  "sort-faculty-name-desc": "Fakultetas pagal pavadinimą mažėjimo tvarka",
                  "sort-university-name-asc": "Universitetas pagal pavadinimą didėjimo tvarka",
                  "sort-university-name-desc": "Universitetas pagal pavadinimą mažėjimo tvarka",
                  "sort-city-asc": "Miestas pagal pavadinimą didėjimo tvarka",
                  "sort-city-desc": "Miestas pagal pavadinimą mažėjimo tvarka",
                  "sort-rating-asc": "Įvertinimas pagal didėjimą",
                  "sort-rating-desc": "Įvertinimas pagal mažėjimą",
                  "access-website": "Prisijungti į svetainę",
                  "profile": "Profilis",
                  "would-recommend-faculty": "Rekomenduotų fakultetą",
                  "would-recommend": "Rekomenduotų",
                  "would-not-recommend": "Nerekomenduotų",
                  "difficulty": "Sunkumas",
                  "review-professors-and-courses": "Profesoriai ir kursai",
                  "review-career-opportunities": "Karjeros galimybės",
                  "review-acommodation": "Apgyvendinimas",
                  "review-student-associations": "Studentų asociacijos",
                  "review-facilities": "Įranga",
                  "review-study-time-vs-free-time": "Studijų laikas vs. laisvas laikas",
                  "review-5-stars": "5 žv.",
                  "review-4-stars": "4 žv.",
                  "review-3-stars": "3 žv.",
                  "review-2-stars": "2 žv.",
                  "review-1-stars": "1 žv.",
                  "faculty-official-presentation": "Oficialus pristatymas",
                  "faculty-general-presentation": "Bendra pristatymas",
                  "faculty-general-presentation-website": "Svetainė",
                  "faculty-general-presentation-address": "Adresas",
                  "faculty-general-presentation-phone": "Telefonas",
                  "faculty-general-presentation-description": "Aprašymas",
                  "faculty-general-presentation-duration": "Trukmė",
                  "faculty-general-presentation-license-places": "Licencijos vietos",
                  "faculty-general-presentation-master-places": "Magistro vietos",
                  "faculty-general-presentation-license-budget-places": "Licencijos biudžeto vietos",
                  "faculty-general-presentation-master-budget-places": "Magistro biudžeto vietos",
                  "faculty-general-presentation-license-tax-places": "Licencijos mokesčio vietos",
                  "faculty-general-presentation-master-tax-places": "Magistro mokesčio vietos",
                  "faculty-general-presentation-license-active-students": "Aktyvūs studentai licencija",
                  "faculty-general-presentation-master-active-students": "Aktyvūs studentai magistro",
                  "faculty-general-presentation-number-of-professors": "Profesorių skaičius",
                  "faculty-general-presentation-students-to-professors-ratio": "Studentų ir profesorių santykis",
                  "faculty-general-presentation-group-contact": "Kontaktai",
                  "faculty-general-presentation-group-about": "Apie",
                  "faculty-general-presentation-group-places": "Laisvos vietos",
                  "faculty-general-presentation-group-stats": "Statistika",
                  "faculty-career-opportunities": "Karjeros galimybės",
                  "other-faculties-same-university": "Kiti fakultetai toje pačioje universitete",
                  "study-programs": "Studijų programos",
                  "study-programs-domain": "Sritis",
                  "study-programs-specialization": "Specializacija",
                  "study-programs-admission": "Priėmimas",
                  "study-programs-accreditation": "Akreditacija",
                  "study-programs-available-places": "Pasiekiamos vietos",
                  "study-programs-budget-places": "Biudžeto vietos",
                  "study-programs-tax-places": "Mokesčio vietos",
                  "study-programs-last-grade": "Paskutinis balas",
                  "study-programs-candidates-per-place": "Kandidatai vienai vietai",
                  "study-programs-annual-tax": "Metinė mokestis",
                  "no-reviews": "Atsiliepimų nėra",
                  "no-more-reviews": "Daugiau atsiliepimų apie šį fakultetą nėra",
                  "compare-faculties": "Palyginti fakultetus",
                  "compare-suggestions": "Palyginimo pasiūlymai",
                  "compare-add-faculty": "Norėdami pradėti, pridėkite kolegiją",
                  "compare-search-faculties": "Fakultetų paieška",
                  "compare-search-placeholder": "Ieškokite fakulteto, kad pridėtumėte jį į lentelę...",
                  "compare-search-no-results": "Rezultatų nerasta",
                  "compare-radar-title": "Vaizdinis palyginimas",
                  "compare-table-title": "Palyginimo lentelė",
                  "compare-radar-max-faculties": "Pastaba: diagrama rodo tik pirmuosius 5 fakultetus (daugiausiai) iš lentelės",
                  "featured-review-label": "Ką sako studentai",
                  "review-filter-user-status": "Filtruoti pagal autoriaus tipą",
                  "expand-all-reviews": "+ Išskleisti visus atsiliepimus",
                  "collapse-all-reviews": "- Suskleisti visus atsiliepimus",
                  "contact-us": "Susisiekite su mumis",
                  "contact-subtitle": "Pasakykite mums savo nuomonę apie Unistart platformą, praneškite apie klaidą arba tiesiog palikite žinutę.",
                  "contact-name": "Vardas",
                  "contact-message": "Žinutė",
                  "contact-send": "Siųsti",
                  "login-header": "Prisijungimas",
                  "login-button-message": "Prisijungti į paskyrą",
                  "login-google": "Prisijungti per Google",
                  "login-account-benefits": "Naudotojo paskyros privalumai",
                  "login-forgot-password": "Pamiršote slaptažodį",
                  "login-send-to-register": "Neturite paskyros? Registruokitės",
                  "register-header": "Sukurti naują paskyrą",
                  "register-confirm-password": "Patvirtinkite slaptažodį",
                  "register-terms-and-conditions": "Perskaičiau ir sutinku su naudojimosi sąlygomis ir asmens duomenų apdorojimo politika",
                  "register-terms-prefix": "Perskaičiau ir sutinku su",
                  "register-terms-link": "naudojimosi sąlygomis",
                  "register-terms-conjunction": "ir",
                  "register-policy-link": "asmens duomenų apdorojimo politika",
                  "register-subscribe": "Noriu gauti naujienas",
                  "register-button-message": "Registruotis",
                  "register-send-to-login": "Jau turite paskyrą? Prisijunkite",
                  "benefits-subtitle": "Sukurkite paskyrą Unistart ir turėsite prieigą prie visų platformos funkcijų.",
                  "benefits-1": "Įvertinkite fakultetus.",
                  "benefits-2": "Išsaugokite mėgstamus fakultetus.",
                  "benefits-3": "(Artimiausiu metu) Užduokite klausimus.",
                  "benefits-4": "Pritaikykite savo paskyrą.",
                  "benefits-5": "Balsuokite už atsiliepimus.",
                  "benefits-6": "(Artimiausiu metu) Pasirinkite gauti pranešimus apie mėgstamus fakultetus.",
                  "benefits-7": "Turėsite prieigą prie būsimų funkcijų, skirtų tik registruotiems naudotojams.",
                  "add-review-general-review-title": "Bendras įvertinimas",
                  "add-review-give-rating": "Pridėti įvertinimą",
                  "add-review-very-unsatisfied": "Labai nepatenkintas",
                  "add-review-unsatisfied": "Nepatenkintas",
                  "add-review-satisfied": "Patenkintas",
                  "add-review-very-satisfied": "Labai patenkintas",
                  "add-review-remaining-characters": " likusios simboliai",
                  "add-review-next-step": "Kitas žingsnis",
                  "add-review-previous-step": "Ankstesnis žingsnis",
                  "add-review-the-step": "Žingsnis",
                  "add-review-engage-text-title": "Įvertink savo patirtį šioje fakultete, 100% anonimiškai",
                  "add-review-engage-text-1": "Prisimink, kaip daug galvojai, kokį fakultetą pasirinkti ir kaip norėtum žinoti, kaip vyksta paskaitos, kaip pasiruošę yra dėstytojai, kokias darbo galimybes turėsi, kokia yra studentų asociacijų situacija ar gyvenimo bendrabutyje sąlygos.",
                  "add-review-engage-text-2": "Dabar turi galimybę pridėti įvertinimą pasirinktam fakultetui ir padėti kitam studentui, kuris nori sekti tuo pačiu keliu. Tavo įvertinimas gali padaryti skirtumą. Tai užtrunka tik minutę.",
                  "add-review-categories-title": "Įvertinimas pagal kategorijas. Šis puslapis yra neprivalomas.",
                  "add-review-would-you-recommend": "Ar rekomenduotum fakultetą?",
                  "add-review-something-about-yourself": "Pasakyk mums kažką apie save",
                  "add-review-manage-to-get-hired": "Ar pavyko įsidarbinti pagal specialybę studijų metu arba per 6 mėnesius po baigimo?",
                  "add-review-post": "Paskelbti įvertinimą",
                  "add-review-agree-to-terms-and-policy-1": "Pridėdamas šį įvertinimą, sutinku",
                  "add-review-agree-to-terms-and-policy-2": "su Platformos.",
                  "add-review-agree-to-terms-and-policy-3": "ir",
                  "add-review-agree-to-terms-and-policy-4": " Paslaugų teikimo sąlygomis ",
                  "add-review-agree-to-terms-and-policy-5": " Privatumo politika ",
                  "add-question-agree-to-terms-and-policy-1": "Pridėdamas šį klausimą, sutinku",
                  "qa-ask-question-title-label": "Pavadinimas",
                  "qa-ask-question-text-label": "Klausimas",
                  "qa-ask-question-title-placeholder": "Parašykite savo klausimo pavadinimą čia. Minimum 5, maksimum 150 simbolių.",
                  "qa-ask-question-text-placeholder": "Parašykite savo klausimą čia. Minimum 10, maksimum 1000 simbolių.",
                  "qa-post-question": "Paskelbti klausimą",
                  "qa-post-answer": "Paskelbti atsakymą",
                  "add-review-success-message": "Jūsų atsiliepimas sėkmingai pridėtas. Per 48 valandas jį galite redaguoti arba ištrinti iš savo paskyros.",
                  "add-review-general-review-placeholder": "Apibūdinkite savo patirtį bent 30 simbolių...",
                  "add-review-professors-and-courses-placeholder": "Kokie yra dėstytojai? Kaip vyksta paskaitos? Kokie yra mokymosi medžiagos?",
                  "add-review-career-opportunities-placeholder": "Nesvarbu, ar baigėte universitetą, ar vis dar esate studentas, apibūdinkite savo patirtį su įsidarbinimo galimybėmis...",
                  "add-review-acommodation-placeholder": "Ar gyvenate bendrabutyje, ar nuomojatės? Apibūdinkite gyvenamąją vietą, kainą, sąlygas...",
                  "add-review-student-associations-placeholder": "Kiek gerai jaučiatės atstovaujami studentų asociacijų? Kokias veiklas ir renginius jie organizuoja?",
                  "add-review-facilities-placeholder": "Apibūdinkite įstaigos patalpas ir jų būklę (biblioteką, auditoriją, laboratorijas, įrangą).",
                  "add-review-study-time-vs-free-time-placeholder": "Kiek valandų per savaitę praleidžiate universitete? Kiek valandų mokotės ar dirbate prie projektų?",
                  "user-profile-title-v1": "Tavo profilis nepilnas!",
                  "user-profile-title-v2": "Tavo profilis pilnas!",
                  "user-profile-subtitle-v1": "Norėdami suteikti tau personalizuotą patirtį, prašome užpildyti savo profilį.",
                  "user-profile-subtitle-v2": "Džiaugiamės galėdami suteikti tau personalizuotą patirtį.",
                  "user-profile-profile-text": "Profilis ir informacija",
                  "user-profile-last-name": "Pavardė",
                  "user-profile-first-name": "Vardas",
                  "user-profile-reset-password": "Atstatyti slaptažodį",
                  "user-profile-city": "Miesto",
                  "user-profile-birthday": "Gimimo data",
                  "user-profile-domains-text": "Kuriuose mokslo srityse norėtum studijuoti?",
                  "user-profile-cities-text": "Kuriuose miestuose norėtum studijuoti?",
                  "user-profile-subscribe-text": "Prenumeruoti naujienlaiškį",
                  "user-profile-saved-faculties": "Išsaugotos fakultetai",
                  "user-profile-saved-faculties-empty": "Nesate išsaugoję jokių fakultetų",
                  "user-profile-notify-text": "Praneškite man, kai pasirodys naujos apžvalgos",
                  "user-profile-my-reviews": "Mano apžvalgos",
                  "user-profile-my-reviews-empty": "Nesate pridėję jokių apžvalgų",
                  "user-profile-saved-companies": "Išsaugotos įmonės",
                  "user-profile-saved-companies-empty": "Nesate išsaugoję jokių įmonių",
                  "user-profile-sign-out": "Atsijungti"
                };

/* ---------------- Cookie consent (Google Analytics) ----------------
   Strictly necessary cookies (session/auth) are unaffected by this and always load.
   Google Analytics only loads after the user explicitly accepts via the banner below,
   or has previously done so. Google Sign-In is handled separately, on demand, in
   googleButtonClick()/loadGoogleSignInScript() above — clicking that button is itself
   the consent action for loading it. */

var COOKIE_CONSENT_STORAGE_KEY = "cookieConsent"; // "accepted" | "rejected"
var GA_MEASUREMENT_ID = "G-W7XYVR8RJ0";

function getCookieConsent() {
	try {
		return localStorage.getItem(COOKIE_CONSENT_STORAGE_KEY);
	} catch (e) {
		return null;
	}
}

function setCookieConsent(value) {
	try {
		localStorage.setItem(COOKIE_CONSENT_STORAGE_KEY, value);
	} catch (e) {}
}

function initCookieConsent() {
	renderCookieConsentBanner();
	var consent = getCookieConsent();
	if (consent === "accepted") {
		loadGoogleAnalytics();
	} else if (consent !== "rejected") {
		showCookieConsentBanner();
	}
}

function renderCookieConsentBanner() {
	if (document.getElementById('cookie-consent-banner')) {
		return;
	}

	if (!document.getElementById('cookie-consent-style')) {
		var style = document.createElement('style');
		style.id = 'cookie-consent-style';
		style.textContent =
			'#cookie-consent-banner{position:fixed;left:0;right:0;bottom:0;z-index:10000;' +
			'background:#1f2937;color:#f5f5f5;padding:16px 24px;display:none;align-items:center;' +
			'justify-content:space-between;flex-wrap:wrap;gap:12px;box-shadow:0 -2px 10px rgba(0,0,0,.25);font-size:14px;}' +
			'#cookie-consent-banner.cookie-consent-visible{display:flex;}' +
			'#cookie-consent-banner a{color:#8ecae6;}' +
			'#cookie-consent-banner .cookie-consent-text{flex:1 1 320px;margin:0;}' +
			'#cookie-consent-banner .cookie-consent-actions{display:flex;gap:10px;flex:0 0 auto;}' +
			'#cookie-consent-banner .cookie-consent-actions button{border:none;border-radius:4px;' +
			'padding:8px 18px;font-size:14px;cursor:pointer;}' +
			'#cookie-consent-accept{background:#2ba84a;color:#fff;}' +
			'#cookie-consent-reject{background:transparent;color:#f5f5f5;border:1px solid #f5f5f5;}' +
			'@media (max-width:480px){#cookie-consent-banner .cookie-consent-actions{width:100%;justify-content:flex-end;}}';
		document.head.appendChild(style);
	}

	var bannerHtml =
		'<div class="cookie-consent-text">' +
			'Folosim cookie-uri strict necesare pentru funcționarea Site-ului. Cu acordul tău, folosim și cookie-uri de analiză (Google Analytics) pentru a înțelege cum este folosit Site-ul. Poți afla mai multe în ' +
			'<a href="./policy.html" target="_blank">Politica de confidențialitate</a>.' +
		'</div>' +
		'<div class="cookie-consent-actions">' +
			'<button id="cookie-consent-reject" type="button" onclick="rejectCookieConsent()">Refuză</button>' +
			'<button id="cookie-consent-accept" type="button" onclick="acceptCookieConsent()">Acceptă</button>' +
		'</div>';

	var banner = document.createElement('div');
	banner.id = 'cookie-consent-banner';
	banner.innerHTML = bannerHtml;
	document.body.appendChild(banner);
}

function showCookieConsentBanner() {
	renderCookieConsentBanner();
	document.getElementById('cookie-consent-banner').classList.add('cookie-consent-visible');
}

function hideCookieConsentBanner() {
	var banner = document.getElementById('cookie-consent-banner');
	if (banner) {
		banner.classList.remove('cookie-consent-visible');
	}
}

function acceptCookieConsent() {
	setCookieConsent('accepted');
	hideCookieConsentBanner();
	loadGoogleAnalytics();
}

function rejectCookieConsent() {
	setCookieConsent('rejected');
	hideCookieConsentBanner();
}

function openCookieSettings(e) {
	if (e) {
		e.preventDefault();
	}
	showCookieConsentBanner();
}

function loadGoogleAnalytics() {
	if (document.getElementById('ga-gtag-script')) {
		return;
	}
	var script = document.createElement('script');
	script.id = 'ga-gtag-script';
	script.async = true;
	script.src = 'https://www.googletagmanager.com/gtag/js?id=' + GA_MEASUREMENT_ID;
	document.head.appendChild(script);

	window.dataLayer = window.dataLayer || [];
	window.gtag = window.gtag || function() { dataLayer.push(arguments); };
	gtag('js', new Date());
	gtag('config', GA_MEASUREMENT_ID);
}

/* ---------------- Deselectable radio buttons ----------------
   Native <input type="radio"> can only be *selected* by a click — clicking the option
   that's already checked does nothing, there's no built-in way back to "nothing
   selected". This makes every radio group on the site deselectable: clicking an
   already-checked option unchecks it.

   Radios here are almost always clicked via their <label for="..."> (the input itself
   is visually hidden by CSS), and a label click only forwards a synthetic `click` to
   its input — never `mousedown`/`mouseup`. So the usual "snapshot .checked on
   mousedown" trick needs to resolve the label back to its input first, or it silently
   never fires for how these controls are actually used. */

function resolveRadioFromEventTarget(target) {
	if (!target) {
		return null;
	}
	if (target.matches && target.matches('input[type="radio"]')) {
		return target;
	}
	if (target.tagName === 'LABEL') {
		if (target.htmlFor) {
			var byId = document.getElementById(target.htmlFor);
			if (byId && byId.matches && byId.matches('input[type="radio"]')) {
				return byId;
			}
		}
		var nested = target.querySelector('input[type="radio"]');
		if (nested) {
			return nested;
		}
	}
	return null;
}

var radioCheckedBeforeInteraction = null;

document.addEventListener('mousedown', function (e) {
	var radio = resolveRadioFromEventTarget(e.target);
	radioCheckedBeforeInteraction = radio ? radio.checked : null;
});

document.addEventListener('click', function (e) {
	var el = e.target;
	if (!(el && el.matches && el.matches('input[type="radio"]'))) {
		return;
	}
	if (radioCheckedBeforeInteraction === true) {
		el.checked = false;
		el.dispatchEvent(new Event('change', { bubbles: true }));
	}
	radioCheckedBeforeInteraction = null;
});

/* ---------------- Pending review draft (write-first, log in later) ----------------
   Lets a user write a review before having an account: if they're not logged in when
   they try to post it, the draft is stashed here and they're prompted to log in/register.
   Read by review.html (to save/restore the draft) and by login()/loginWithSocialMediaAccount()
   above (to redirect back to the right faculty's review page after a successful login,
   regardless of which page that login happened on — e.g. after email confirmation on
   login.html, which normally redirects home). */

var PENDING_REVIEW_STORAGE_PREFIX = "pendingReview_";
var PENDING_REVIEW_MAX_AGE_MS = 3 * 24 * 60 * 60 * 1000; // 3 days

function savePendingReviewDraft(facultyId, reviewData) {
	try {
		var payload = {
			review: reviewData,
			savedAt: Date.now()
		};
		localStorage.setItem(PENDING_REVIEW_STORAGE_PREFIX + facultyId, JSON.stringify(payload));
	} catch (e) {}
}

// Returns null (and silently discards the entry) if the draft is missing or too old —
// a login that happens weeks after a draft was abandoned shouldn't surprise-redirect
// the user back to it.
function getPendingReviewDraft(facultyId) {
	try {
		var raw = localStorage.getItem(PENDING_REVIEW_STORAGE_PREFIX + facultyId);
		if (!raw) {
			return null;
		}
		var draft = JSON.parse(raw);
		if (!draft.savedAt || (Date.now() - draft.savedAt) > PENDING_REVIEW_MAX_AGE_MS) {
			clearPendingReviewDraft(facultyId);
			return null;
		}
		return draft;
	} catch (e) {
		return null;
	}
}

function clearPendingReviewDraft(facultyId) {
	try {
		localStorage.removeItem(PENDING_REVIEW_STORAGE_PREFIX + facultyId);
	} catch (e) {}
}

// Returns the facultyId of any pending, still-fresh draft found in storage, or null.
// There should realistically only ever be one at a time (a new draft save doesn't
// clear a previous, unrelated one, but that's an acceptable edge case rather than
// something worth building multi-draft handling for).
function findAnyPendingReviewFacultyId() {
	try {
		for (var i = 0; i < localStorage.length; i++) {
			var key = localStorage.key(i);
			if (key && key.indexOf(PENDING_REVIEW_STORAGE_PREFIX) === 0) {
				var facultyId = key.substring(PENDING_REVIEW_STORAGE_PREFIX.length);
				if (getPendingReviewDraft(facultyId)) { // validates freshness, clears if stale
					return facultyId;
				}
			}
		}
	} catch (e) {}
	return null;
}

// Called right after a successful login/registration, from whichever page it happened
// on. Sends the user back to finish a pending review if one exists; otherwise falls
// back to the normal behavior (reload the current page, or go home if login/register
// happened on a dedicated page).
function redirectAfterLogin() {
	var pendingFacultyId = findAnyPendingReviewFacultyId();
	if (pendingFacultyId) {
		window.location.href = "./review.html?faculty=" + pendingFacultyId;
		return;
	}

	var currentLocationPath = window.location.pathname;
	if (currentLocationPath.includes("login") || currentLocationPath.includes("register")) {
		var urlHomepageRedirect = "./";
		window.location.replace(urlHomepageRedirect);
	} else {
		location.reload();
	}
}

function isAdminPortalPage() {
	return window.location.pathname.indexOf('admin-portal') !== -1;
}

$(function() {
	// Internal admin tool, not public-facing — no analytics/consent banner there.
	if (!isAdminPortalPage()) {
		initCookieConsent();
	}
});