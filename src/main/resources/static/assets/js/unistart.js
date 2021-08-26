// $.ajaxSetup({
	// beforeSend: function (request) {
		// console.log("ss");
		// var jwtToken = getUField("ut");
		// if (jwtToken) {
			 // request.setRequestHeader("Token","Bearer " + jwtToken);
		// }  
    // }
// });

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

// document.getElementById("login-email").oninput = function () {
	// document.getElementById('error-login-email').innerHTML = "";
// }

// document.getElementById("login-pass").oninput = function () {
	// document.getElementById('error-login').innerHTML = "";
// }

// document.getElementById("register-email").oninput = function () {
	// document.getElementById('error-register-email').innerHTML = "";
// }

// document.getElementById("register-pass").oninput = function () {
	// document.getElementById('error-register').innerHTML = "";
// }

// document.getElementById("register-confirm-pass").oninput = function () {
	// document.getElementById('error-register').innerHTML = "";
// }


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
				savedFacultiesIds.push(savedFaculties[i]);
			}
			var likedReviewsIds = response.result.likedReviews;
			var addedReviews = response.result.addedReviews;
			
			setUField("ut", jwtToken);
			setUField("usf", savedFacultiesIds);
			setUField("ulr", likedReviewsIds);
			setUField("uar", addedReviews);
			
			location.reload();
		},
		error: function(error) {
			if (error.status == 401) {
				errorLogin.innerHTML = "Datele de autentificare sunt incorecte.";
			} else {
				errorLogin.innerHTML = "A apărut o eroare. Te rugăm să încerci din nou mai târziu.";
			}
		
			document.getElementById("login-button").disabled = false;
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
	var errorLogin = document.getElementById('error-register');
	
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
		errorLogin.innerHTML = "Trebuie să accepți Termenii și condițiile de utilizare și Politica de prelucrare a datelor";
		isInvalid = true;
	} else {
		acceptTermsAndConditions = true;
	}
	
	if (!userPass) {
		errorLogin.innerHTML = "Câmp obligatoriu";
		isInvalid = true;
	} else if (userPass.length < passMin) {
		errorLogin.innerHTML = "Parola trebuie să fie de minim 7 caractere.";
		isInvalid = true;
	} else if (!validatePass(userPass)) {
		errorLogin.innerHTML = "Parola trebuie să conțină litere și cifre.";
		isInvalid = true;
	} else if (userPass != userConfPass) {
		errorLogin.innerHTML = "Cele două parole nu se potrivesc";
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
			location.reload();
		},
		error: function(error) {
			if (error.status == 401) {
				errorLogin.innerHTML = "Datele de autentificare sunt incorecte.";
			} else {
				errorLogin.innerHTML = "A apărut o eroare. Te rugăm să încerci din nou mai târziu.";
			}
		
			document.getElementById("register-button").disabled = false; 
		}
	});
	
}

function logout() {
	localStorage.removeItem("u");
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
			el.setAttribute("data-toggle", 'modal');
			el.setAttribute("data-target", '#general-modal');
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
				el.setAttribute("data-toggle", 'modal');
				el.setAttribute("data-target", '#general-modal');
				document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation-triangle fa-3x"></i>';
				document.getElementById('modal-text').innerHTML = "Evaluarea nu a putut fi votată";
			}
		}
	});

}

function reportReview(el) {

	var regularIcon = "far";
	var solidIcon = "fas";
	
	if ($(el).hasClass(regularIcon)) {
		
		el.setAttribute("data-toggle", 'modal');
		el.setAttribute("data-target", '#general-modal');
		document.getElementById('modal-text').innerHTML = "Se procesează...";

		var reviewId = $(el).parent().parent().parent().attr("data-reviewid");
		
		var backendUrl = new URL(backendUrlRoot + "/v1/review/" + reviewId + "/report");
		$.ajax({
			url: backendUrl,
			type: 'PUT',
			dataType: 'json',
			contentType: 'application/json',
			crossDomain: true,
			success: function () {
				document.getElementById('modal-text').innerHTML = "Evaluarea a fost raportată";
				$(el).removeClass(regularIcon);
				$(el).addClass(solidIcon);
			},
			error: function(error) {
				if (checkLoggedInUser(error.status)) {
					document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation-triangle fa-3x"></i>';
					document.getElementById('modal-text').innerHTML = "Evaluarea nu a putut fi raportată";
				}
			}
		});
	
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
				var urlHomepageRedirect = "./";
				window.location.replace(urlHomepageRedirect);
			// });
		} else {
			// document.getElementById('modal-header-text').innerHTML = '<i class="fas fa-exclamation fa-2x"></i>';
			// document.getElementById('modal-text').innerHTML = "Este nevoie de autentificare pentru a folosi această funcționalitate.";
			// document.getElementById('general-modal-footer').innerHTML = '<button type="button" class="btn btn-warning btn-simple" data-dismiss="modal" style="color: orange;">Autentificare</button>';
			// $('#general-modal').modal('show');
			// $("#general-modal").on("hidden.bs.modal", function () {
				var urlHomepageRedirect = "./";
				window.location.replace(urlHomepageRedirect);
			// });
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

