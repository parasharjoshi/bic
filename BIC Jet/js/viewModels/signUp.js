/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * login module
 */
define(['ojs/ojcore', 'knockout', 'jquery', 'appController', 'restClient', 'ojs/ojinputtext', 'ojs/ojlabel', 'ojs/ojbutton', 'ojs/ojformlayout'
], function (oj, ko, $, app, client) {
    /**
     * The view model for the main content view template
     */
    function signupContentViewModel() {
        var self = this;
        this.firstName = ko.observable(null);
        this.lastName = ko.observable(null);
        this.emailId = ko.observable(null);
        this.userName = ko.observable(null);
        this.password = ko.observable(null);
        this.repassword = ko.observable(null);
        this.enableCheck = ko.computed(function () {
            return !(this.userName());
        }, this);
        this.userNameAvailability = ko.observable(null);
        this.userNameMessage = ko.observable(null);
        this.errorMessage = ko.observable(null);
        this.signupSuccess = ko.observable(false);
        this.enableSignupForm = ko.computed(function () {
            return !(this.signupSuccess());
        }, this);
        this.isSmall = oj.ResponsiveKnockoutUtils.createMediaQueryObservable(
                oj.ResponsiveUtils.getFrameworkQuery(oj.ResponsiveUtils.FRAMEWORK_QUERY_KEY.SM_ONLY));

        // For small screens: labels on top
        // For medium screens and up: labels inline
        this.labelEdge = ko.computed(function () {
            return this.isSmall() ? "top" : "start";
        }, this);

        self.submit = function () {
            self.errorMessage("Please wait...!");
            if (!self.userName() || !self.password() || !self.repassword() || !self.firstName() || !self.lastName() || !self.emailId()) {
                self.errorMessage("Please provide values for all fields...");
            } else {
                //Invoke the rest call
                //Without promise
                /*client.invokePost(app.hostBasePath() + '/login',
                 {"userName": self.userName(), "password": self.password()},
                 self.handleSuccess, self.handleFailure);
                 */
                if (self.password() === self.repassword()) {
                    client.invokePost(app.hostBasePath() + '/signup',
                            {userName: self.userName(), password: self.password(), firstName: self.firstName(), lastName: self.lastName(), emailId: self.emailId()})
                            .done(function (data) {
                                self.handleSuccess(data);
                            })
                            .fail(function (xhr, status, error) {
                                self.handleFailure(xhr, status, error);
                            });
                } else {
                    self.errorMessage("Password and Confirmed password do not match. Try again...");
                }
            }
        };

        self.checkUserName = function () {
            console.log("Checking username " + self.userName());
            document.getElementById('checkUsername').disabled = true;
            client.invokeGet(app.hostBasePath() + '/checkUsername/' + self.userName())
                    .done(function (data) {
                        console.log("done...");
                        //self.parseModelInfo(data);

                        if (data.available === true) {
                            self.userNameAvailability(true);
                            self.userNameMessage("Username is available.");
                        } else {
                            self.userNameAvailability(false);
                            self.userNameMessage("Oops!! The username is already taken.");
                        }
                    })
                    .fail(function (xhr, status, error) {
                        console.log("fail...");
                        self.errorMessage("Check failed. Could not verify. Please contact admin if problem persists.");
                        self.initializing(false);
                    });

        };

        self.handleSuccess = function (data) {
            if (data.error && data.error.errorCode) {
                self.errorMessage(data.error.errorCode + ' : ' + data.error.errorMessage);
            } else if (data) {
                if (data.active && !data.locked) {
                    self.signupSuccess(true);
                }
            } else {
                self.errorMessage("Something went wrong. Please try later..!");
            }
        };

        self.handleFailure = function (xhr, status, error) {
            if (xhr.status && xhr.statusText) {
                self.errorMessage(xhr.status + ' : ' + xhr.statusText + " - Something went wrong. Please try later..!");
            } else {
                self.errorMessage("Something went wrong. Please try later..!");
            }
            app.userLogin(null);
            app.isAdmin(null);
        };

        self.usernameChange = function (data) {
            console.log("In value change");
            document.getElementById('checkUsername').disabled = false;
            self.userNameAvailability(null);
            self.userNameMessage(null);
        };

        self.reset = function () {
            self.userName(null);
            self.password(null);
            self.repassword(null);
            self.errorMessage(null);
            self.firstName(null);
            self.lastName(null);
            self.emailId(null);
            self.userNameAvailability(null);
            self.userNameMessage(null);
        };

        self.goToLogin = function () {
            app.router.go('login');
        };
    }

    return signupContentViewModel;
});
