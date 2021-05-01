/**
 * Interceptors
 */
angular
    .module('smartcloudserviceApp')
    .factory('alertHandlerInterceptor', alertHandlerInterceptor)
    .factory('alertErrorHandlerInterceptor', alertErrorHandlerInterceptor)
    .factory('authExpiredInterceptor', authExpiredInterceptor)
    .factory('authInterceptor', authInterceptor);


function alertHandlerInterceptor ($q, AlertUtils) {
    return {
        response: response
    };

    function response (response) {
        var alertKey = response.headers('X-Success-Message');
        if (angular.isString(alertKey)) {
            AlertUtils.success(decodeURIComponent(alertKey), { param : decodeURIComponent(alertKey)});
        }
        alertKey = response.headers('X-Warn-Message');
        if (angular.isString(alertKey)) {
            AlertUtils.warning(decodeURIComponent(alertKey), { param : decodeURIComponent(alertKey)});
        }
        return response;
    }
}
function alertErrorHandlerInterceptor ($q, $rootScope) {
    return {
        responseError: responseError
    };

    function responseError (response) {
        if (!(response.status === 401 && (response.data === '' || (response.data.path && response.data.path.indexOf('/api/account/user') > -1 )))) {
            // 非访问受限401错误并且访问非用户账号接口时
            $rootScope.$emit('smartcloudserviceApp.httpError', response);
        }
        return $q.reject(response);
    }
}
function authExpiredInterceptor($rootScope, $q, $injector, $localStorage, $sessionStorage) {
    return {
        responseError: responseError
    };

    function responseError(response) {
        if (response.status === 401) {
            delete $localStorage.authenticationToken;
            delete $sessionStorage.authenticationToken;
            var PrincipalService = $injector.get('PrincipalService');
            if (PrincipalService.isAuthenticated()) {
                var AuthenticationService = $injector.get('AuthenticationService');
                AuthenticationService.authorize(true);
            }
        }
        return $q.reject(response);
    }
}
function authInterceptor ($rootScope, $q, $location, $localStorage, $sessionStorage) {
    return {
        request: request
    };

    function request (config) {
        /*jshint camelcase: false */
        config.headers = config.headers || {};
        var token = $localStorage.authenticationToken || $sessionStorage.authenticationToken;
        
        if (token && token.expires_at && token.expires_at > new Date().getTime()) {
            config.headers.Authorization = 'Bearer ' + token.access_token;
        }

        // Generate request ID
        config.headers['traceId'] = 'T' + Date.now() + (Math.random() * 100000).toFixed();
        return config;
    }
}