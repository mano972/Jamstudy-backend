
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
		document.getElementById('myaccount').innerHTML = '<i class="far fa-user" style="margin-right: 7px"></i>Contul meu';
	} else {
		document.getElementById('myaccount').innerHTML = '<i class="far fa-user" style="margin-right: 7px"></i>Autentificare';	
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
				var likedReviewsIds = userResponse.likedReviews;
				var addedReviews = userResponse.addedReviews;
				
				setUField("usf", savedFacultiesIds);
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
		loginWithFb(response);
    });
}

function loginWithFb(userData) {
	
	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/loginfb");
	
	var email = userData.email;
	var firstName = userData.first_name;
	var lastName = userData.last_name;
		
	var body = {
		email: email,
		firstName: firstName,
		lastName: lastName
	};
	
	if (document.getElementById("login-button")) {
		document.getElementById("login-button").disabled = true; 
	}
	if (document.getElementById("register-button")) {
		document.getElementById("register-button").disabled = true; 
	}
	document.getElementById("login-fb-button").disabled = true; 
	
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
			var likedReviewsIds = response.result.likedReviews;
			var addedReviews = response.result.addedReviews;
			
			setUField("ut", jwtToken);
			setUField("usf", savedFacultiesIds);
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
			}
			if (document.getElementById("register-button")) {
				document.getElementById("register-button").disabled = false; 
			}
			document.getElementById("login-fb-button").disabled = false; 
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
	document.getElementById("login-fb-button").disabled = true; 
	
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
			var likedReviewsIds = response.result.likedReviews;
			var addedReviews = response.result.addedReviews;
			
			setUField("ut", jwtToken);
			setUField("usf", savedFacultiesIds);
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
			document.getElementById("login-fb-button").disabled = false;
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
	document.getElementById("login-fb-button").disabled = true; 
	
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
			document.getElementById('modal-text').innerHTML = "Un email cu instrucțiuni a fost trimis la adresa de email introdusă.";
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
			document.getElementById("login-fb-button").disabled = false; 			
		}
	});
	
}

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

function resendConfirmationEmail(token) {
	var backendUrl = new URL(backendUrlRoot + "/v1/userprofile/resendconfirmation");
		
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
		icon.nextSibling.innerHTML = "Vezi Comparaţii";
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

