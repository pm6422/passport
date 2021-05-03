/**
 * Services
 */
angular
    .module('smartcloudserviceApp')
    .factory('StateHandler', StateHandler)
    .factory('Base64Utils', Base64Utils)
    .factory('ParseLinksUtils', ParseLinksUtils)
    .factory('PaginationUtils', PaginationUtils)
    .factory('AlertUtils', AlertUtils)
    .factory('DateUtils', DateUtils)
    .factory('DataUtils', DataUtils)
    .factory('ProfileService', ProfileService)
    .factory('PasswordService', PasswordService)
    .factory('PasswordResetInitService', PasswordResetInitService)
    .factory('PasswordResetFinishService', PasswordResetFinishService)
    .factory('MetricsService', MetricsService)
    .factory('HealthService', HealthService)
    .factory('ConfigurationService', ConfigurationService)
    .factory('HttpSessionService', HttpSessionService)
    .factory('DictService', DictService)
    .factory('DictItemService', DictItemService)
    .factory('AuditsService', AuditsService)
    .factory('TrackerService', TrackerService)
    .factory('LoggerService', LoggerService)
    .factory('AccountService', AccountService)
    .factory('RegisterService', RegisterService)
    .factory('ActivateService', ActivateService)
    .factory('PrincipalService', PrincipalService)
    .factory('AuthServerService', AuthServerService)
    .factory('AuthenticationService', AuthenticationService)
    .factory('AuthorityAdminMenuService', AuthorityAdminMenuService)
    .factory('AppService', AppService)
    .factory('AuthorityService', AuthorityService)
    .factory('AppAuthorityService', AppAuthorityService)
    .factory('UserService', UserService)
    .factory('OAuth2ClientService', OAuth2ClientService)
    .factory('OAuth2AccessTokenService', OAuth2AccessTokenService)
    .factory('OAuth2RefreshTokenService', OAuth2RefreshTokenService)
    .factory('OAuth2ApprovalService', OAuth2ApprovalService)
    .factory('AdminMenuService', AdminMenuService);

/**
 * StateHandler
 */
function StateHandler($rootScope, $state, $sessionStorage, $window, AuthenticationService, PrincipalService, AlertUtils, APP_NAME, VERSION) {
    return {
        initialize: initialize
    };

    function initialize() {
        $rootScope.APP_NAME = APP_NAME;
        $rootScope.VERSION = VERSION;

        var stateChangeStart = $rootScope.$on('$stateChangeStart', function (event, toState, toStateParams, fromState) {
            $rootScope.toState = toState;
            $rootScope.toStateParams = toStateParams;
            $rootScope.fromState = fromState;

            // Redirect to a state with an external URL (http://stackoverflow.com/a/30221248/1098564)
            if (toState.external) {
                event.preventDefault();
                $window.open(toState.url, '_self');
            }

            if (PrincipalService.isIdentityResolved()) {
                AuthenticationService.authorize();
            }
        });

        var stateChangeSuccess = $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
            var titleKey = APP_NAME;

            // Set the page title key to the one configured in state or use default one
            if (toState.data.pageTitle) {
                titleKey = toState.data.pageTitle;
            }
            $window.document.title = titleKey;
        });

        var cleanHttpErrorListener = $rootScope.$on('smartcloudserviceApp.httpError', function (event, httpResponse) {
            var i;
            event.stopPropagation();
            switch (httpResponse.status) {
                // connection refused, server not reachable
                case 0:
                    AlertUtils.error('Server not reachable');
                    break;
                case 400:
                    if (httpResponse.data && httpResponse.data.errorFields) {
                        for (i = 0; i < httpResponse.data.errorFields.length; i++) {
                            var fieldError = httpResponse.data.errorFields[i];
                            AlertUtils.error(fieldError.message);
                        }
                    } else if (httpResponse.data && httpResponse.data.message) {
                        AlertUtils.error(httpResponse.data.message);
                    } else {
                        AlertUtils.error(httpResponse.data);
                    }
                    break;
                case 404:
                    AlertUtils.error('Not found');
                    break;
                default:
                    if (httpResponse.data && httpResponse.data.message) {
                        AlertUtils.error(httpResponse.data.message);
                    } else {
//	                    $state.go('error', {errorMessage: angular.toJson(httpResponse) });
                        $state.go('error', {errorMessage: httpResponse});
                    }
            }
        });

        $rootScope.$on('$destroy', function () {
            if (angular.isDefined(stateChangeStart) && stateChangeStart !== null) {
                stateChangeStart();
            }
            if (angular.isDefined(stateChangeSuccess) && stateChangeSuccess !== null) {
                stateChangeSuccess();
            }
            if (angular.isDefined(cleanHttpErrorListener) && cleanHttpErrorListener !== null) {
                cleanHttpErrorListener();
            }
        });
    }
}
/**
 * Base64Utils
 */
function Base64Utils() {
    var keyStr = 'ABCDEFGHIJKLMNOP' +
        'QRSTUVWXYZabcdef' +
        'ghijklmnopqrstuv' +
        'wxyz0123456789+/' +
        '=';

    return {
        decode: decode,
        encode: encode
    };

    function encode(input) {
        var output = '',
            chr1, chr2, chr3 = '',
            enc1, enc2, enc3, enc4 = '',
            i = 0;

        while (i < input.length) {
            chr1 = input.charCodeAt(i++);
            chr2 = input.charCodeAt(i++);
            chr3 = input.charCodeAt(i++);

            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;

            if (isNaN(chr2)) {
                enc3 = enc4 = 64;
            } else if (isNaN(chr3)) {
                enc4 = 64;
            }

            output = output +
                keyStr.charAt(enc1) +
                keyStr.charAt(enc2) +
                keyStr.charAt(enc3) +
                keyStr.charAt(enc4);
            chr1 = chr2 = chr3 = '';
            enc1 = enc2 = enc3 = enc4 = '';
        }

        return output;
    }

    function decode(input) {
        var output = '',
            chr1, chr2, chr3 = '',
            enc1, enc2, enc3, enc4 = '',
            i = 0;

        // remove all characters that are not A-Z, a-z, 0-9, +, /, or =
        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, '');

        while (i < input.length) {
            enc1 = keyStr.indexOf(input.charAt(i++));
            enc2 = keyStr.indexOf(input.charAt(i++));
            enc3 = keyStr.indexOf(input.charAt(i++));
            enc4 = keyStr.indexOf(input.charAt(i++));

            chr1 = (enc1 << 2) | (enc2 >> 4);
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
            chr3 = ((enc3 & 3) << 6) | enc4;

            output = output + String.fromCharCode(chr1);

            if (enc3 !== 64) {
                output = output + String.fromCharCode(chr2);
            }
            if (enc4 !== 64) {
                output = output + String.fromCharCode(chr3);
            }

            chr1 = chr2 = chr3 = '';
            enc1 = enc2 = enc3 = enc4 = '';
        }

        return output;
    }
}
/**
 * ParseLinksUtils
 */
function ParseLinksUtils() {
    return {
        parse: parse
    };

    function parse(header) {
        if (header.length === 0) {
            throw new Error('input must not be of zero length');
        }

        // Split parts by comma
        var parts = header.split(',');
        var links = {};
        // Parse each part into a named link
        angular.forEach(parts, function (p) {
            var section = p.split(';');
            if (section.length !== 2) {
                throw new Error('section could not be split on ";"');
            }
            var url = section[0].replace(/<(.*)>/, '$1').trim();
            var queryString = {};
            url.replace(
                new RegExp('([^?=&]+)(=([^&]*))?', 'g'),
                function ($0, $1, $2, $3) {
                    queryString[$1] = $3;
                }
            );
            var page = queryString.page;
            if (angular.isString(page)) {
                page = parseInt(page);
            }
            var name = section[1].replace(/rel="(.*)"/, '$1').trim();
            links[name] = page;
        });

        return links;
    }
}
/**
 * PaginationUtils
 */
function PaginationUtils() {
    return {
        parseAscending: parseAscending,
        parsePage: parsePage,
        parsePredicate: parsePredicate
    };

    function parseAscending(sort) {
        var sortArray = sort.split(',');
        if (sortArray.length > 1) {
            return sort.split(',').slice(-1)[0] === 'asc';
        } else {
            // default to true if no sort defined
            return true;
        }
    }

    // query params are strings, and need to be parsed
    function parsePage(page) {
        return parseInt(page);
    }

    // sort can be in the format `id,asc` or `id`
    function parsePredicate(sort) {
        var sortArray = sort.split(',');
        if (sortArray.length > 1) {
            sortArray.pop();
        }
        return sortArray.join(',');
    }
}
/**
 * AlertUtils
 */
function AlertUtils(SweetAlert, toaster, APP_NAME) {
    return {
        success: success,
        error: error,
        warning: warning,
        createDeleteConfirmation: createDeleteConfirmation,
        createResetPasswordConfirmation: createResetPasswordConfirmation
    };

    function success(msg, params, position) {
        toaster.success(APP_NAME, msg);
    }

    function error(msg, params, position) {
        toaster.error(APP_NAME, msg);
    }

    function warning(msg, params, position) {
        toaster.warning(APP_NAME, msg);
    }

    function createDeleteConfirmation(alerText, confirmDelete) {
        SweetAlert.swal({
            title: '确定删除?',
            text: alerText ? alerText : '',
            type: 'warning',
            animation: 'slide-from-top',
            showCancelButton: true,
            confirmButtonColor: '#ec4758',
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            closeOnConfirm: true,
            closeOnCancel: true
        }, confirmDelete);
    }

    function createResetPasswordConfirmation(alerText, confirmReset) {
        SweetAlert.swal({
            title: '确定重置密码?',
            text: alerText ? alerText : '',
            type: 'warning',
            animation: 'slide-from-top',
            showCancelButton: true,
            confirmButtonColor: '#ec4758',
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            closeOnConfirm: true,
            closeOnCancel: true
        }, confirmReset);
    }
}
/**
 * DateUtils
 */
function DateUtils($filter) {
    return {
        convertDateTimeFromServer: convertDateTimeFromServer,
        convertLocalDateFromServer: convertLocalDateFromServer,
        convertLocalDateToServer: convertLocalDateToServer,
        dateformat: dateformat
    };

    function convertDateTimeFromServer(date) {
        if (date) {
            return new Date(date);
        } else {
            return null;
        }
    }

    function convertLocalDateFromServer(date) {
        if (date) {
            var dateString = date.split('-');
            return new Date(dateString[0], dateString[1] - 1, dateString[2]);
        }
        return null;
    }

    function convertLocalDateToServer(date) {
        if (date) {
            return $filter('date')(date, 'yyyy-MM-dd');
        } else {
            return null;
        }
    }

    function dateformat() {
        return 'yyyy-MM-dd';
    }
}
/**
 * DataUtils
 */
function DataUtils($window) {
    return {
        abbreviate: abbreviate,
        byteSize: byteSize,
        openFile: openFile,
        toBase64: toBase64
    };

    function abbreviate(text) {
        if (!angular.isString(text)) {
            return '';
        }
        if (text.length < 30) {
            return text;
        }
        return text ? (text.substring(0, 15) + '...' + text.slice(-10)) : '';
    }

    function byteSize(base64String) {
        if (!angular.isString(base64String)) {
            return '';
        }

        function endsWith(suffix, str) {
            return str.indexOf(suffix, str.length - suffix.length) !== -1;
        }

        function paddingSize(base64String) {
            if (endsWith('==', base64String)) {
                return 2;
            }
            if (endsWith('=', base64String)) {
                return 1;
            }
            return 0;
        }

        function size(base64String) {
            return base64String.length / 4 * 3 - paddingSize(base64String);
        }

        function formatAsBytes(size) {
            return size.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ') + ' bytes';
        }

        return formatAsBytes(size(base64String));
    }

    function openFile(type, data) {
        $window.open('data:' + type + ';base64,' + data, '_blank', 'height=300,width=400');
    }

    function toBase64(file, cb) {
        var fileReader = new FileReader();
        fileReader.readAsDataURL(file);
        fileReader.onload = function (e) {
            var base64Data = e.target.result.substr(e.target.result.indexOf('base64,') + 'base64,'.length);
            cb(base64Data);
        };
    }
}
/**
 * ProfileService
 */
function ProfileService($q, $http, $localStorage) {
    var dataPromise;

    return {
        getProfileInfo: getProfileInfo
    };

    function getProfileInfo() {
        if (angular.isUndefined(dataPromise)) {
            dataPromise = $http.get('open-api/system/profile-info').then(function (result) {
                if (result.data.activeProfiles) {
                    return result.data;
                }
            });
        }
        return dataPromise;
    }
}
/**
 * PasswordService
 */
function PasswordService($resource) {
    return $resource('api/account/password', {}, {
        'update': {method: 'PUT'}
    });
}
/**
 * PasswordResetInitService
 */
function PasswordResetInitService($resource) {
    return $resource('open-api/account/reset-password/init', {}, {
        'create': {method: 'POST'}
    });
}
/**
 * PasswordResetFinishService
 */
function PasswordResetFinishService($resource) {
    return $resource('open-api/account/reset-password/finish', {}, {
        'create': {method: 'POST'}
    });
}

/**
 * MetricsService
 */
function MetricsService($rootScope, $http) {
    return {
        getMetrics: getMetrics,
        threadDump: threadDump
    };

    function getMetrics() {
        return $http.get('api/metric/metrics').then(function (response) {
            return response.data;
        });
    }

    function threadDump() {
        return $http.get('management/threaddump').then(function (response) {
            return response.data;
        });
    }
}

/**
 * HealthService
 */
function HealthService($rootScope, $http) {
    var separator = '.';
    return {
        checkHealth: checkHealth,
        transformHealthData: transformHealthData,
        getBaseName: getBaseName,
        getSubSystemName: getSubSystemName
    };

    function checkHealth() {
        return $http.get('management/health').then(function (response) {
            return response.data;
        });
    }

    function transformHealthData(data) {
        var response = [];
        flattenHealthData(response, null, data.components);
        return response;
    }

    function getBaseName(name) {
        if (name) {
            var split = name.split('.');
            return split[0];
        }
    }

    function getSubSystemName(name) {
        if (name) {
            var split = name.split('.');
            split.splice(0, 1);
            var remainder = split.join('.');
            return remainder ? ' - ' + remainder : '';
        }
    }

    /* private methods */
    function flattenHealthData(result, path, data) {
        angular.forEach(data, function (value, key) {
            if (isHealthObject(value)) {
                if (hasSubSystem(value)) {
                    addHealthObject(result, false, value, getModuleName(path, key));
                    flattenHealthData(result, getModuleName(path, key), value);
                } else {
                    addHealthObject(result, true, value, getModuleName(path, key));
                }
            }
        });
        return result;
    }

    function addHealthObject(result, isLeaf, healthObject, name) {

        var healthData = {
            'name': name
        };
        var details = {};
        var hasDetails = false;

        angular.forEach(healthObject, function (value, key) {
            if (key === 'status' || key === 'error') {
                healthData[key] = value;
            } else {
                if (!isHealthObject(value)) {
                    details[key] = value;
                    hasDetails = true;
                }
            }
        });

        // Add the of the details
        if (hasDetails) {
            angular.extend(healthData, {'details': details});
        }

        // Only add nodes if they provide additional information
        if (isLeaf || hasDetails || healthData.error) {
            result.push(healthData);
        }
        return healthData;
    }

    function getModuleName(path, name) {
        var result;
        if (path && name) {
            result = path + separator + name;
        } else if (path) {
            result = path;
        } else if (name) {
            result = name;
        } else {
            result = '';
        }
        return result;
    }

    function hasSubSystem(healthObject) {
        var result = false;
        angular.forEach(healthObject, function (value) {
            if (value && value.status) {
                result = true;
            }
        });
        return result;
    }

    function isHealthObject(healthObject) {
        var result = false;
        angular.forEach(healthObject, function (value, key) {
            if (key === 'status') {
                result = true;
            }
        });
        return result;
    }
}

function ConfigurationService($filter, $http, APP_NAME) {
    var service = {
        get: get,
        getEnv: getEnv
    };

    return service;

    function get() {
        return $http.get('management/configprops').then(getConfigPropsComplete);

        function getConfigPropsComplete(response) {
            var properties = [];
            var propertiesObject = getConfigPropertiesObjects(response.data);
            for (var key in propertiesObject) {
                if (propertiesObject.hasOwnProperty(key)) {
                    properties.push(propertiesObject[key]);
                }
            }
            var orderBy = $filter('orderBy');
            return orderBy(properties, 'prefix');
        }
    }

    function getConfigPropertiesObjects(res) {
        // This code is for Spring Boot 2
        if (res['contexts'] !== undefined) {
            for (var key in res['contexts']) {
                // If the key is not bootstrap, it will be the ApplicationContext Id
                // For default app, it is baseName
                // For microservice, it is baseName-1
                if (!key.startsWith('bootstrap')) {
                    return res['contexts'][key]['beans'];
                }
            }
        }
        // by default, use the default ApplicationContext Id
        return res['contexts'][APP_NAME]['beans'];
    }

    function getEnv() {
        return $http.get('management/env').then(getEnvComplete);

        function getEnvComplete(response) {
            var properties = {};
            angular.forEach(response.data['propertySources'], function (val, key) {
                var vals = [];
                angular.forEach(val['properties'], function (v, k) {
                    vals.push({key: k, val: v});
                });
                properties[val['name']] = vals;
            });
            return properties;
        }
    }
}

function HttpSessionService($resource) {
    var service = $resource('api/http-sessions/:id', {}, {
        'query': {method: 'GET', isArray: true},
        'del': {method: 'DELETE'}
    });
    return service;
}

function DictService($resource) {
    var service = $resource('api/dicts/:id', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        },
        'create': {method: 'POST'},
        'update': {method: 'PUT'},
        'del': {method: 'DELETE'}
    });
    return service;
}

function DictItemService($resource) {
    var service = $resource('api/dict-items/:id', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        },
        'create': {method: 'POST'},
        'update': {method: 'PUT'},
        'del': {method: 'DELETE'}
    });
    return service;
}

function AuditsService($resource) {
    var service = $resource('api/user-audit-events/:id', {}, {
        'get': {method: 'GET', isArray: true},
        'query': {
            method: 'GET',
            isArray: true,
            params: {fromDate: null, toDate: null}
        }
    });

    return service;
}

function TrackerService($rootScope, $window, $cookies, $http, $q, AuthServerService) {
    var stompClient = null;
    var subscriber = null;
    var listener = $q.defer();
    var connected = $q.defer();
    var alreadyConnectedOnce = false;

    var service = {
        connect: connect,
        disconnect: disconnect,
        receive: receive,
        sendActivity: sendActivity,
        subscribe: subscribe,
        unsubscribe: unsubscribe
    };

    return service;

    function connect() {
        //building absolute path so that websocket doesn't fail when deploying with a context path
        var loc = $window.location;
        var url = '//' + loc.host + loc.pathname + 'websocket/tracker';
        var authToken = AuthServerService.getToken();
        if (authToken) {
            url += '?access_token=' + authToken.access_token;
        }
        var socket = new SockJS(url);
        stompClient = Stomp.over(socket);
        var stateChangeStart;
        var headers = {};
        stompClient.connect(headers, function () {
            connected.resolve('success');
            sendActivity();
            if (!alreadyConnectedOnce) {
                stateChangeStart = $rootScope.$on('$stateChangeStart', function () {
                    sendActivity();
                });
                alreadyConnectedOnce = true;
            }
        });
        $rootScope.$on('$destroy', function () {
            if (angular.isDefined(stateChangeStart) && stateChangeStart !== null) {
                stateChangeStart();
            }
        });
    }

    function disconnect() {
        if (stompClient !== null) {
            stompClient.disconnect();
            stompClient = null;
        }
    }

    function receive() {
        return listener.promise;
    }

    function sendActivity() {
        if (stompClient !== null && stompClient.connected) {
            stompClient
                .send('/topic/server-subscriber/tracker',
                    {},
                    angular.toJson({'page': $rootScope.toState.name}));
        }
    }

    function subscribe() {
        connected.promise.then(function () {
            subscriber = stompClient.subscribe('/topic/client-subscriber/tracker', function (data) {
                listener.notify(angular.fromJson(data.body));
            });
        }, null, null);
    }

    function unsubscribe() {
        if (subscriber !== null) {
            subscriber.unsubscribe();
        }
        stompClient = null;
        listener = $q.defer();
        connected = $q.defer();
    }
}

function LoggerService($resource) {
    var service = $resource('management/loggers/:name', {}, {
        'query': {method: 'GET'},
        'changeLevel': {method: 'POST'}
    });

    return service;
}
/**
 * AccountService
 */
function AccountService($resource) {
    var service = $resource('api/account/:extension', {}, {
        'get': {
            method: 'GET', params: {extension: 'user'},
            interceptor: {
                response: function (response) {
                    // expose response
                    return response;
                }
            }
        },
        'create': {method: 'POST', params: {extension: 'user'}},
        'update': {
            method: 'PUT', params: {extension: 'user'}
        },
        'queryAuthorityNames': {method: 'GET', isArray: true, params: {extension: 'authority-names'}}
    });
    return service;
}
/**
 * RegisterService
 */
function RegisterService($resource) {
    return $resource('open-api/account/register', {}, {
        'create': {method: 'POST'}
    });
}
/**
 * ActivateService
 */
function ActivateService($resource) {
    return $resource('open-api/account/activate/:key', {}, {
        'get': {method: 'GET', params: {}, isArray: false}
    });
}
/**
 * PrincipalService
 */
function PrincipalService($q, $http, AccountService, TrackerService) {
    var _identity;
    var _authenticated = false;

    return {
        authenticate: authenticate,
        hasAnyAuthority: hasAnyAuthority,
        hasAuthority: hasAuthority,
        identity: identity,
        isAuthenticated: isAuthenticated,
        isIdentityResolved: isIdentityResolved
    };

    function authenticate(identity) {
        _identity = identity;
        _authenticated = identity !== null;
    }

    function hasAnyAuthority(authorities) {
        if (!_authenticated || !_identity || !_identity.authorities) {
            return false;
        }

        for (var i = 0; i < authorities.length; i++) {
            if (_identity.authorities.indexOf(authorities[i]) !== -1) {
                return true;
            }
        }

        return false;
    }

    function hasAuthority(authority) {
        if (!_authenticated) {
            return $q.when(false);
        }

        return this.identity().then(function (_id) {
            return _id.authorities && _id.authorities.indexOf(authority) !== -1;
        }, function () {
            return false;
        });
    }

    function identity(force) {
        var deferred = $q.defer();

        if (force === true) {
            _identity = undefined;
        }

        // check and see if we have retrieved the identity data from the server.
        // if we have, reuse it by immediately resolving
        if (angular.isDefined(_identity)) {
            deferred.resolve(_identity);

            return deferred.promise;
        }

        // retrieve the identity data from the server, update the identity object, and then resolve.
//      AccountService.get({}, getAccountThen, getAccountCatch);
        $http.get('open-api/account/user').then(function (response) {
            if (response.data.userName) {
                getAccountThen(response);
            } else {
                getAccountCatch();
            }
        }, getAccountCatch);

        function getAccountThen(response) {
            _identity = response.data;
            _authenticated = true;
            deferred.resolve(_identity);
            TrackerService.connect();
        }

        function getAccountCatch() {
            _identity = null;
            _authenticated = false;
            deferred.resolve(_identity);
        }

        return deferred.promise;
    }

    function isAuthenticated() {
        return _authenticated;
    }

    function isIdentityResolved() {
        return angular.isDefined(_identity);
    }
}
/**
 * AuthServerService
 */
function AuthServerService($http, $localStorage) {
    return {
        getToken: getToken,
        getAccessToken: getAccessToken,
        getRefreshToken: getRefreshToken,
        login: login,
        logout: logout
    };

    function getToken() {
        return $localStorage.authenticationToken;
    }

    function getAccessToken() {
        return $localStorage.authenticationToken != null ? $localStorage.authenticationToken.access_token : null;
    }

    function getRefreshToken() {
        return $localStorage.authenticationToken != null ? $localStorage.authenticationToken.refresh_token : null;
    }

    function login(credentials, successCallback, errorCallback) {
        $http.get('open-api/oauth2-client/internal-client').then(function (response) {
            var data = 'username=' + encodeURIComponent(credentials.userName)
                + '&password=' + encodeURIComponent(credentials.password)
                + '&grant_type=password'
                + '&client_id=' + response.data.first
                + '&client_secret=' + response.data.second;

            return $http.post('oauth/token', data, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Accept': 'application/json'
                }
            }).success(function (data) {
                var expiredAt = new Date();
                expiredAt.setSeconds(expiredAt.getSeconds() + data.expires_in);
                data.expires_at = expiredAt.getTime();
                $localStorage.authenticationToken = data;
                successCallback(data);
            }).error(function (data) {
                errorCallback(data);
            });
        });
    }

    function logout() {
        $http.post('api/account/logout').then(function () {
            delete $localStorage.authenticationToken;
        });
    }
}
/**
 * AuthenticationService
 */
function AuthenticationService($rootScope, $state, $sessionStorage, $q, $location, PrincipalService, AuthServerService) {
    return {
        authorize: authorize,
        getPreviousState: getPreviousState,
        login: login,
        logout: logout,
        resetPreviousState: resetPreviousState,
        storePreviousState: storePreviousState
    };

    function authorize(force, authThen) {
        return PrincipalService.identity(force).then(function (account) {
            var isAuthenticated = PrincipalService.isAuthenticated();

            // Callback function
            if (authThen) {
                authThen();
            }

            // an authenticated user can't access to login and register pages
//            if (isAuthenticated && $rootScope.toState.parent === 'account' && ($rootScope.toState.name === 'login' || $rootScope.toState.name === 'register')) {
            if (isAuthenticated && ($rootScope.toState.name === 'login' || $rootScope.toState.name === 'register')) {
                // redirect to dashboard
                $location.path('/');
            }

            // recover and clear previousState after external login redirect (e.g. oauth2)
            if (isAuthenticated && !$rootScope.fromState.name && getPreviousState()) {
                var previousState = getPreviousState();
                resetPreviousState();
                $state.go(previousState.name, previousState.params);
            }

//            if ($rootScope.toState.data.authorities && $rootScope.toState.data.authorities.length > 0 && !PrincipalService.hasAnyAuthority($rootScope.toState.data.authorities)) {
//                if (isAuthenticated) {
//                    // user is signed in but not authorized for desired state
//                    $state.go('accessdenied');
//                }
//                else {
//                    // user is not authenticated. stow the state they wanted before you
//                    // send them to the login service, so you can return them when you're done
//                    storePreviousState($rootScope.toState.name, $rootScope.toStateParams);
//
//                    // now, send them to the signin state so they can log in
//                    $state.go('accessdenied').then(function() {
//                        LoginService.open();
//                    });
//                }
//            }

            if ($rootScope.toState.data.authorities && $rootScope.toState.data.authorities.length > 0 && !PrincipalService.hasAnyAuthority($rootScope.toState.data.authorities)) {
                if (isAuthenticated) {
                    // user is signed in but not authorized for desired state
                    $state.go('accessdenied');
                }
                else {
                    // user is not authenticated. stow the state they wanted before you
                    // send them to the login service, so you can return them when you're done
                    storePreviousState($rootScope.toState.name, $rootScope.toStateParams);

                    // now, send them to the signin state so they can log in
                    $state.go('accessdenied').then(function () {
                        $state.go('login');
                    });
                }
            }
        });
    }

    function login(credentials, successCallback, errorCallback) {
        var that = this;
        AuthServerService.login(credentials,
            function (data) {
                PrincipalService.identity(true).then(function (account) {
                    successCallback(data);
                });
            },
            function (data) {
                that.logout();
                errorCallback(data);
            });
    }

    function logout() {
        AuthServerService.logout();
        PrincipalService.authenticate(null);
    }

    function getPreviousState() {
        var previousState = $sessionStorage.previousState;
        return previousState;
    }

    function resetPreviousState() {
        delete $sessionStorage.previousState;
    }

    function storePreviousState(previousStateName, previousStateParams) {
        var previousState = {'name': previousStateName, 'params': previousStateParams};
        $sessionStorage.previousState = previousState;
    }
}
/**
 * AuthorityAdminMenuService
 */
function AuthorityAdminMenuService($resource) {
    return $resource('api/authority-admin-menus/:extension', {}, {
        'query': {method: 'GET', isArray: true},
        'update': {
            method: 'PUT',
            isArray: true,
            interceptor: {
                response: function (response) {
                    return response;
                }
            }
        },
        'queryUserMenus': {method: 'GET', isArray: true, params: {extension: 'user-menus'}},
        'queryUserLinks': {method: 'GET', isArray: true, params: {extension: 'user-links'}}
    });
}
/**
 * AppService
 */
function AppService($resource) {
    var service = $resource('api/apps/:name', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        },
        'create': {method: 'POST'},
        'update': {method: 'PUT'},
        'del': {method: 'DELETE'}
    });
    return service;
}
/**
 * AuthorityService
 */
function AuthorityService($resource) {
    var service = $resource('api/authorities/:name', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        },
        'create': {method: 'POST'},
        'update': {method: 'PUT'},
        'del': {method: 'DELETE'}
    });
    return service;
}
/**
 * AppAuthorityService
 */
function AppAuthorityService($resource) {
    var service = $resource('api/app-authorities/:id', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        },
        'create': {method: 'POST'},
        'update': {method: 'PUT'},
        'del': {method: 'DELETE'}
    });
    return service;
}
/**
 * UserService
 */
function UserService($resource) {
    var service = $resource('api/users/:userName', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        },
        'create': {method: 'POST'},
        'update': {method: 'PUT'},
        'del': {method: 'DELETE'},
        'resetPassword': {method: 'PUT', params: {userName: '@userName'}}
    });
    return service;
}

/**
 * OAuth2ClientService
 */
function OAuth2ClientService($resource) {
    var service = $resource('api/oauth2-clients/:id', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        },
        'create': {method: 'POST'},
        'update': {method: 'PUT'},
        'del': {method: 'DELETE'}
    });
    return service;
}

/**
 * OAuth2AccessTokenService
 */
function OAuth2AccessTokenService($resource) {
    var service = $resource('api/oauth2-access-tokens/:id', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        },
        'del': {method: 'DELETE'}
    });
    return service;
}

/**
 * OAuth2RefreshTokenService
 */
function OAuth2RefreshTokenService($resource) {
    var service = $resource('api/oauth2-refresh-tokens/:id', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        },
        'del': {method: 'DELETE'}
    });
    return service;
}

/**
 * OAuth2ApprovalService
 */
function OAuth2ApprovalService($resource) {
    var service = $resource('api/oauth2-approvals/:id', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        },
        'del': {method: 'DELETE'}
    });
    return service;
}
/**
 * AdminMenuService
 */
function AdminMenuService($resource) {
    var service = $resource('api/admin-menus/:extension/:id', {}, {
        'query': {method: 'GET', isArray: true},
        'queryParents': {method: 'GET', isArray: true, params: {extension: 'parents'}},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        },
        'create': {method: 'POST'},
        'update': {method: 'PUT'},
        'del': {method: 'DELETE'},
        'moveUp': {method: 'PUT', params: {extension: 'move-up', id: '@id'}},
        'moveDown': {method: 'PUT', params: {extension: 'move-down', id: '@id'}}
    });
    return service;
}

