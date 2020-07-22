import http from "k6/http";
import {
  randomSeed
} from "k6";
import {
  check,
  sleep
} from 'k6';
import {
  Trend,
  Counter
} from 'k6/metrics';
export let options = {
    hosts:{
        'server': '136.159.209.204'
    },
    // vus: ARG_VUS,
    // duration: 'ARG_DURATIONs',
    vus: 20,
    duration: '20s',
    userAgent: 'MyK6UserAgentString/1.0',
};

let login_duration_trend = new Trend('login_duration_trend');
let edit_book_duration_trend = new Trend('edit_book_duration_trend');
let get_book_duration_trend = new Trend('get_book_duration_trend');

let login_counter = new Counter('login_counter');
let edit_book_counter = new Counter('edit_book_counter');
let get_book_counter = new Counter('get_book_counter');

export function setup() {
}

export default function (data) {

    let uniqueNumber = __VU * 100000000 + __ITER;
    randomSeed(uniqueNumber);
    // console.log(`VU: ${__VU}  -  ITER: ${__ITER}`);
    // const AuthProb = ARG_AuthProb;
    // const BookProb = ARG_BookProb;
    // const SLEEP_DURATION = ARG_SLEEP_DURATION;
    const AuthProb = 0.5;
    const BookProb = 0.5;
    const SLEEP_DURATION = 5;
  
    const execute_edit_book = function () {
      let edit_book_params = {
        headers: {
          'debug_id': new Date().getTime()
        },
        tags: {
          name: "edit_book"
        }
      };
      let edit_book_response = http.get(
        'http://server:9080/editbook',
        edit_book_params,
      );
      edit_book_duration_trend.add(edit_book_response.timings.duration);
      edit_book_counter.add(1);
      check(edit_book_response, {
        'is_edit_book_200': r => r.status === 200
      });
    }
    const execute_get_book = function () {
      let get_book_params = {
        headers: {
          'debug_id': new Date().getTime()
        },
        tags: {
          name: "get_book"
        }
      };
      let get_book_response = http.get(
        'http://server:9080/getbook',
        get_book_params
      );
      get_book_duration_trend.add(get_book_response.timings.duration);
      get_book_counter.add(1);
      check(get_book_response, {
        'is_get_book_200': r => r.status === 200
      });
    }
    const execute_login = function () {  
      let login_params = {
        headers: {
          'debug_id': new Date().getTime(),
        },
        tags: {
          name: "login"
        }
      };
  
      let login_response = http.get(
        'http://server:9080/login',
        login_params
      );
      login_duration_trend.add(login_response.timings.duration);
      login_counter.add(1);
      check(login_response, {
        'is_login_200': r => r.status === 200,
      });
    }
    const r = Math.random();
    const sTime = Math.random() * SLEEP_DURATION + 0.5 * SLEEP_DURATION;
    sleep(sTime);
    if (r < AuthProb) {
        execute_login();
    } else if (r >= AuthProb && r < AuthProb + BookProb) {
      if (Math.random() > 0.5) {
        execute_edit_book();
      } else {
        execute_get_book();
      }
    }
};

export function teardown(data) {}