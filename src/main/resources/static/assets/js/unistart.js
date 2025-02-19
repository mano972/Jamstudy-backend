
$.ajaxSetup({
  headers: {
    Token: getUField("ut") ? "Bearer " + getUField("ut") : null
  }
});

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
		document.getElementById('myaccount').innerHTML = '<i class="far fa-user" style="margin-right: 7px" data-i18n-key="header-footer-my-account"></i>Contul meu';
	} else {
		document.getElementById('myaccount').innerHTML = '<i class="far fa-user" style="margin-right: 7px" data-i18n-key="header-footer-login"></i>Autentificare';
	}
}

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
				var addedReviews = userResponse.addedReviews;
				
				setUField("usf", savedFacultiesIds);
				setUField("usc", savedCompaniesIds);
				setUField("ulr", likedReviewsIds);
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

function googleButtonClick(e) {
	e.preventDefault();

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
			var addedReviews = response.result.addedReviews;
			
			setUField("ut", jwtToken);
			setUField("usf", savedFacultiesIds);
			setUField("usc", savedCompaniesIds);
			setUField("ulr", likedReviewsIds);
			setUField("uar", addedReviews);
			
			var currentLocationPath = window.location.pathname;
			if (currentLocationPath.includes("login") || currentLocationPath.includes("register")) {
				var urlHomepageRedirect = "./";
				window.location.replace(urlHomepageRedirect);
			} else {
				location.reload();
			}
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

// Logout from facebook
function fbLogout() {
    FB.logout(function() {
    });
}

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
			var addedReviews = response.result.addedReviews;
			
			setUField("ut", jwtToken);
			setUField("usf", savedFacultiesIds);
			setUField("usc", savedCompaniesIds);
			setUField("ulr", likedReviewsIds);
			setUField("uar", addedReviews);
			
			var currentLocationPath = window.location.pathname;
			if (currentLocationPath.includes("login")) {
				var urlHomepageRedirect = "./";
				window.location.replace(urlHomepageRedirect);
			} else {
				location.reload();
			}
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
			document.getElementById('modal-text').innerHTML = "Parola a fost schimbată cu success.";
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
			document.getElementById('modal-text').innerHTML = "Adresa de email a fost confirmată cu success. Te poți autentifica.";
		},
		error: function(error) {
			$("#general-modal").modal();
			document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation-triangle fa-3x"></i>';
			if (error.responseJSON) {
				if (error.responseJSON.control) {
					var errorDescription = error.responseJSON.control.errorDescription;
					document.getElementById('modal-text').innerHTML = errorDescription;
				} else {
					document.getElementById('modal-text').innerHTML = "Adresa de email nu a putut fi confirmată.";
				}
			} else {
				document.getElementById('modal-text').innerHTML = "Adresa de email nu a putut fi confirmată.";
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
					document.getElementById('modal-text').innerHTML = "A apărut o eroare.";
				}
			} else {
				document.getElementById('modal-text').innerHTML = "A apărut o eroare.";
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
	FB.getLoginStatus(function(response) {
        if (response.status === 'connected') {
            fbLogout();
        }
    });
	var urlHomepageRedirect = "./";
	window.location.replace(urlHomepageRedirect);
}

function validateEmail(email) {
    const re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}

function validatePass(pass) {
	var lettersNumbers = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{7,}$/;
	return lettersNumbers.test(String(pass).toLowerCase());
}

function goToAddReview(el, facultyId) {
	increaseReviewStatistic(0);
	gtag('event', 'add_review', {
	  'step': '0',
	  'page_name': el.baseURI
	});
	var jwtToken = getUField("ut");
	if (!jwtToken) {
		el.setAttribute("data-toggle", 'modal');
		el.setAttribute("data-target", '#login-modal');
		return false;
	}
	
	var addReviewRedirectUrl = "./review.html?faculty=" + facultyId;
	location.href = addReviewRedirectUrl;
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

			  fetch(backendUrl)
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
			return ' <li ' + props + '>	<div>	 ' + result.facultyName + '	</div>	<small class="text-muted">	' + result.universityName + '	</small></li>	'
		},
		getResultValue: result => result.facultyName,
		onSubmit: result => {
			var urlProfileRedirect = "./profile.html?id=" + result.facultyId;
			location.href = urlProfileRedirect;
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
	var urlFacultyRedirect = "./profile.html?id=" + facultyId + "#reviews";
	location.href = urlFacultyRedirect;
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
                '<option id="c17" value="Alba Iulia; Alba">Alba Iulia</option>';

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
                '<option id="c17" value="Alba Iulia; Alba">Alba Iulia</option>';

    } else if (currentUrl.includes("unistart.lt")) {
    var citiesString =
                '<select id="top-by-city" class="selectpicker" data-style="form-control" data-menu-style="" onchange="loadTopFaculties()">'+
                '<option id= "all" value="Toate">Visi miestai</option>'+
                '<option id="c1" value="Vilnius">Vilnius</option>'+
                '<option id="c2" value="Kaunas">Kaunas</option>'+
                '<option id="c3" value="Klaipėda">Klaipėda</option>'+
                '<option id="c4" value="Šiauliai">Šiauliai</option>'+
                '<option id="c5" value="Panevėžys">Panevėžys</option>';

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
                        '									<input id="register-pass" type="password" class="form-control" placeholder="Password" maxlength="50" oninput="clearErrorRegisterPass()">'+
                        '									<i id="pass-info" data-toggle="tooltip" title="Parola trebuie să aibă minim 7 caractere și să conțină litere și cifre." class="fas fa-info-circle inner-icon" aria-hidden="true"></i>'+
                        '								</fieldset>'+
                        '								<input id="register-confirm-pass" type="password" class="form-control" placeholder="Confirm password" maxlength="50" oninput="clearErrorRegisterConfirmPass()">'+
                        '								<small id="error-register" class="error-message"></small>'+

                        '								<div>'+
                        '									<label class="checkbox" for="policy-check"><input type="checkbox" name="policy" value="" id="policy-check" data-toggle="checkbox"><span data-i18n-key="register-terms-and-conditions">Am citit și înțeles Termenii și condițiile de utilizare și Politica de prelucrare a datelor cu caracter personal</span></label>'+
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
		'                <a id="myaccount" class="btn myaccount-button" onclick="myAccount(this)" data-i18n-key="header-footer-login"><i class="far fa-user" style="margin-right: 7px"></i>Contul meu</a>\n' +
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



// Languages

const ro_json = {
                    "search-v1": "Caută",
                    "search-v2": "Căutare",
                    "sort": "Sortare",
                    "add-review": "Adaugă evaluare",
                    "add-review-tooltip": "Adaugă evaluare 100% anonim",
                    "see-all": "+ Vezi tot",
                    "see-all-v2": "Vezi tot",
                    "see-less": "- Vezi mai puțin",
                    "save": "Salvează",
                    "saved": "Salvată",
                    "compare": "Compară",
                    "see-comparison": "Vezi Comparaţii",
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

                    "compare-faculties": "Compară facultăți",
                    "compare-suggestions": "Sugestii de comparație",
                    "compare-add-faculty": "Adaugă o facultate pentru a începe",
                    "compare-search-faculties": "Caută Facultăți",

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
                    "add-review-general-review-placeholder": "Descrie experiența ta în cel puțin 30 de caractere...",
                    "add-review-professors-and-courses-placeholder": "Cum sunt profesorii? Cum se desfasoară orele? Cum sunt materialele de studiu?",
                    "add-review-career-opportunities-placeholder": "Fie că ai terminat facultatea sau ești încă student, descrie experiența ta legată de oportunitățile de angajare...",
                    "add-review-acommodation-placeholder": "Locuiești în cămin sau în chirie? Descrie locuința, prețul, condițiile...",
                    "add-review-student-associations-placeholder": "Cât de reprezentat te simți de asociațiile studențesti? Ce fel de activități si evenimente organizează?",
                    "add-review-facilities-placeholder": "Descrie facilitățile si starea lor (biblioteca, aula, laboratoarele, echipamentele).",
                    "add-review-study-time-vs-free-time-placeholder": "Câte ore petreci pe săptamână la facultate? Câte ore studiezi sau lucrezi la proiecte?"
                };

const en_json = {
                  "search-v1": "Search",
                  "search-v2": "Search",
                  "sort": "Sort",
                  "add-review": "Add Review",
                  "add-review-tooltip": "Add 100% anonymous review",
                  "see-all": "+ See all",
                  "see-all-v2": "See all",
                  "see-less": "- See less",
                  "save": "Save",
                  "saved": "Saved",
                  "compare": "Compare",
                  "see-comparison": "See Comparisons",
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

                  "compare-faculties": "Compare faculties",
                  "compare-suggestions": "Comparison suggestions",
                  "compare-add-faculty": "Add a faculty to start",
                  "compare-search-faculties": "Search for Faculties",

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
                  "add-review-general-review-placeholder": "Describe your experience in at least 30 characters...",
                  "add-review-professors-and-courses-placeholder": "What are the professors like? How are the classes conducted? What are the study materials like?",
                  "add-review-career-opportunities-placeholder": "Whether you have graduated or are still a student, describe your experience with employment opportunities...",
                  "add-review-acommodation-placeholder": "Do you live in a dormitory or rent? Describe the accommodation, price, conditions...",
                  "add-review-student-associations-placeholder": "How well represented do you feel by the student associations? What kind of activities and events do they organize?",
                  "add-review-facilities-placeholder": "Describe the facilities and their condition (library, auditorium, laboratories, equipment).",
                  "add-review-study-time-vs-free-time-placeholder": "How many hours do you spend per week at university? How many hours do you study or work on projects?"

                };

const lt_json = {
                  "search-v1": "Ieškoti",
                  "search-v2": "Ieškoti",
                  "sort": "Rūšiavimas",
                  "add-review": "Pridėti atsiliepimą",
                  "add-review-tooltip": "Pridėkite 100% anonimišką atsiliepimą",
                  "see-all": "+ Peržiūrėti viską",
                  "see-all-v2": "Peržiūrėti viską",
                  "see-less": "- Mažiau matyti",
                  "save": "Išsaugoti",
                  "saved": "Išsaugota",
                  "compare": "Palyginti",
                  "see-comparison": "Peržiūrėti palyginimus",
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
                  "domain-art-v1": "Menas, Architektūra ir Urbanistika",
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

                  "compare-faculties": "Palyginti fakultetus",
                  "compare-suggestions": "Palyginimo pasiūlymai",
                  "compare-add-faculty": "Norėdami pradėti, pridėkite kolegiją",
                  "compare-search-faculties": "Fakultetų paieška",

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
                  "add-review-general-review-placeholder": "Apibūdinkite savo patirtį bent 30 simbolių...",
                  "add-review-professors-and-courses-placeholder": "Kokie yra dėstytojai? Kaip vyksta paskaitos? Kokie yra mokymosi medžiagos?",
                  "add-review-career-opportunities-placeholder": "Nesvarbu, ar baigėte universitetą, ar vis dar esate studentas, apibūdinkite savo patirtį su įsidarbinimo galimybėmis...",
                  "add-review-acommodation-placeholder": "Ar gyvenate bendrabutyje, ar nuomojatės? Apibūdinkite gyvenamąją vietą, kainą, sąlygas...",
                  "add-review-student-associations-placeholder": "Kiek gerai jaučiatės atstovaujami studentų asociacijų? Kokias veiklas ir renginius jie organizuoja?",
                  "add-review-facilities-placeholder": "Apibūdinkite įstaigos patalpas ir jų būklę (biblioteką, auditoriją, laboratorijas, įrangą).",
                  "add-review-study-time-vs-free-time-placeholder": "Kiek valandų per savaitę praleidžiate universitete? Kiek valandų mokotės ar dirbate prie projektų?"
                };