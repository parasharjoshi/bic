/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * login module
 */
define(['ojs/ojcore', 'knockout', 'jquery', 'appController', 'restClient', 'ojs/ojinputtext', 'ojs/ojlabel', 'ojs/ojbutton'
], function (oj, ko, $, app, client) {
    /**
     * The view model for the main content view template
     */
    function loginContentViewModel() {
        var self = this;
        this.userName = ko.observable(null);
        this.password = ko.observable(null);

        this.errorMessage = ko.observable(null);

        self.submit = function () {
            self.errorMessage("Please wait...!");
            if (!self.userName() || !self.password()) {
                self.errorMessage("Please provide values for both fields...");
            } else {
                //Invoke the rest call
                //Without promise
                /*client.invokePost(app.hostBasePath() + '/login',
                 {"userName": self.userName(), "password": self.password()},
                 self.handleSuccess, self.handleFailure);
                 */
                client.invokePost(app.hostBasePath() + '/login',
                        {userName: self.userName(), password: self.password()})
                        .done(function (data) {
                            self.handleSuccess(data);
                        })
                        .fail(function (xhr, status, error) {
                            self.handleFailure(xhr, status, error);
                        });
            }
        };

        self.handleSuccess = function (data) {
            if (data.error && data.error.errorCode) {
                self.errorMessage(data.error.errorCode + ' : ' + data.error.errorMessage);
            } else if (data) {
                if (data.userName && data.adminUser != null) {
                    app.userLogin(data.userName);
                    app.isAdmin(data.adminUser);
                }
                if(data.lastName && data.firstName)
                app.userFullName(data.firstName+' '+data.lastName);
                app.router.go('home');
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

        self.reset = function () {
            self.userName(null);
            self.password(null);
            self.errorMessage(null);
        };
        
        self.signup = function () {
            app.router.go('signup');
        };
    }

    return loginContentViewModel;
});
