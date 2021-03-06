"use strict";
/**
 * Copyright 2020 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.enableLogger = exports.disableLogger = exports.init = void 0;
const utils_1 = require("./utils");
function init() {
    return utils_1.asyncExec('HMSLocationKit', 'init', []);
}
exports.init = init;
function disableLogger() {
    return utils_1.asyncExec('HMSLocationKit', 'disableLogger', []);
}
exports.disableLogger = disableLogger;
function enableLogger() {
    return utils_1.asyncExec('HMSLocationKit', 'enableLogger', []);
}
exports.enableLogger = enableLogger;
//# sourceMappingURL=HMSLocationKit.js.map