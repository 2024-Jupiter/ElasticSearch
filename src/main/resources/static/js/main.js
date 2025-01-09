(function ($) {
  'use strict';

  // Dropdown on mouse hover
  $(document).ready(function () {
    function toggleNavbarMethod() {
      if ($(window).width() > 992) {
        $('.navbar .dropdown')
        .on('mouseover', function () {
          $('.dropdown-toggle', this).trigger('click');
        })
        .on('mouseout', function () {
          $('.dropdown-toggle', this).trigger('click').blur();
        });
      } else {
        $('.navbar .dropdown').off('mouseover').off('mouseout');
      }
    }

    toggleNavbarMethod();
    $(window).resize(toggleNavbarMethod);
  });

  // Back to top button
  $(window).scroll(function () {
    if ($(this).scrollTop() > 100) {
      $('.back-to-top').fadeIn('slow');
    } else {
      $('.back-to-top').fadeOut('slow');
    }
  });
  $('.back-to-top').click(function () {
    $('html, body').animate({scrollTop: 0}, 1500, 'easeInOutExpo');
    return false;
  });

  // Vendor carousel
  // $('.vendor-carousel').owlCarousel({
  //   loop: true,
  //   margin: 29,
  //   nav: false,
  //   autoplay: true,
  //   smartSpeed: 1000,
  //   responsive: {
  //     0: {
  //       items: 2,
  //     },
  //     576: {
  //       items: 3,
  //     },
  //     768: {
  //       items: 4,
  //     },
  //     992: {
  //       items: 5,
  //     },
  //     1200: {
  //       items: 6,
  //     },
  //   },
  // });

  // Related carousel
  // $('.related-carousel').owlCarousel({
  //   loop: true,
  //   margin: 29,
  //   nav: false,
  //   autoplay: true,
  //   smartSpeed: 1000,
  //   responsive: {
  //     0: {
  //       items: 1,
  //     },
  //     576: {
  //       items: 2,
  //     },
  //     768: {
  //       items: 3,
  //     },
  //     992: {
  //       items: 4,
  //     },
  //   },
  // });

  // Product Quantity
  $('.quantity button').on('click', function () {
    var button = $(this);
    var oldValue = button.parent().parent().find('input').val();
    if (button.hasClass('btn-plus')) {
      var newVal = parseFloat(oldValue) + 1;
    } else {
      if (oldValue > 0) {
        var newVal = parseFloat(oldValue) - 1;
      } else {
        newVal = 0;
      }
    }
    button.parent().parent().find('input').val(newVal);
  });
})(jQuery);

const swalConfig = {confirmButtonColor: '#1f9bcf'};

// pwd, pwd2 일치여부 확인
document.getElementById('pwd2').addEventListener('keyup', checkPw);

function checkPw() {
  let password = document.getElementById('pwd').value;
  let password2 = document.getElementById('pwd2').value;
  let chk = document.getElementById('chk');

  if (password === password2) {
    chk.innerHTML =
        '<i class="fa-regular fa-circle-check"></i>' +
        '&nbsp; 비밀번호가 일치합니다.';
    chk.style.color = 'green';
  }

  if (password != password2) {
    chk.innerHTML =
        '<i class="fa-solid fa-triangle-exclamation"></i>' +
        '&nbsp; 비밀번호가 일치하지 않습니다.';
    chk.style.color = 'red';
  }
}

// 휴대폰 번호 유효성 체크, 인증번호 메시지 전송

document.getElementById('authNumReq').addEventListener('click', sendNumber);

function sendNumber() {
  let phone = document.getElementById('phone').value.trim();
  const phoneReExp = /^(01[016789]{1})-?[0-9]{3,4}-?[0-9]{4}$/;

  if (!phone) {
    Swal.fire({
      icon: 'warning',
      text: '휴대폰 번호를 입력해 주세요.',
      ...swalConfig,
    });
    return;
  }

  if (phoneReExp.test(phone)) {
    const cleanedPhoneNum = phone.replace(/-/g, '');

    $.ajax({
      url: '/api/send-sms/send-authcode',
      type: 'POST',
      data: {phoneNumber: cleanedPhoneNum},
      dataType: 'json',
      success: function (data) {
        console.log(data);
        if (data.state === 'success') {
          Swal.fire({
            icon: 'success',
            html: '입력하신 휴대폰 번호로 <br> 인증번호 6자리가 전송되었습니다.',
            ...swalConfig,
          });
          document.getElementById('authNumRes').removeAttribute('disabled');
          document.getElementById('authNumReq').setAttribute('disabled', true);
          document.getElementById('phone').setAttribute('readonly', true);
        } else {
          Swal.fire({
            icon: 'warning',
            text: response.message || '인증번호 전송에 실패했습니다.',
            ...swalConfig,
          });
        }
      },
      error: function () {
        Swal.fire({
          icon: 'error',
          text: '메시지 발송 중 문제가 발생했습니다. 다시 시도해 주세요.',
          ...swalConfig,
        });
      },
    });
  } else {
    Swal.fire({
      icon: 'warning',
      text: '유효하지 않은 휴대폰 번호입니다.',
      ...swalConfig,
    });
  }
}

// 이메일 유효성, 중복 검사
document
.getElementById('checkEmailBtn')
.addEventListener('click', isEmailAvailable);

function isEmailAvailable() {
  let email = document.getElementById('email').value.trim();
  let emailRegExp =
      /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*\.[a-zA-Z]{2,3}$/i;

  if (!email) {
    Swal.fire({
      icon: 'warning',
      text: '이메일을 입력해 주세요.',
      ...swalConfig,
    });
    return;
  }

  if (!emailRegExp.test(email)) {
    Swal.fire({
      icon: 'warning',
      text: '이메일 형식이 올바르지 않습니다.',
      ...swalConfig,
    });
    return;
  }

  $.ajax({
    url: '/api/users/check-email',
    type: 'GET',
    data: {email: email},
    dataType: 'json',
    success: function (response) {

      if (!response) {
        Swal.fire({
          icon: 'error',
          text: '서버 응답이 올바르지 않습니다.',
          ...swalConfig,
        });
        return;
      }

      if (response.status === 'error') {
        Swal.fire({
          icon: 'warning',
          text: response.errorMessage,
          ...swalConfig,
        });
      }

      if (response.status === 'success') {
        Swal.fire({
          icon: 'success',
          text: response.data.message,
          showCancelButton: true,
          cancelButtonColor: '#d33',
          confirmButtonText: '사용',
          cancelButtonText: '취소',
          ...swalConfig,
        }).then((result) => {
          if (result.isConfirmed) {
            document
            .getElementById('checkEmailBtn')
            .setAttribute('data-available', 'true');
            document
            .getElementById('checkEmailBtn')
            .setAttribute('disabled', true);
          }
        });
      }
    },
    error: function () {
      Swal.fire({
        icon: 'error',
        text: '서버 응답이 올바르지 않습니다.',
        ...swalConfig,
      });
    },
  });
}

// 이메일 재입력 시 중복 검사 버튼 활성화
document.getElementById('email').addEventListener('input', function () {
  if (document.getElementById('checkEmailBtn').getAttribute('data-available')
      === 'true') {
    document.getElementById('checkEmailBtn').removeAttribute('disabled');
    document
    .getElementById('checkEmailBtn')
    .setAttribute('data-available', 'false');
  }
});

// 닉네임 유효성, 중복 검사
document
.getElementById('checkNicknameBtn')
.addEventListener('click', isNicknameAvailable);

function isNicknameAvailable() {
  let nickname = document.getElementById('nickname').value.trim();

  if (!nickname) {
    Swal.fire({
      icon: 'warning',
      text: '닉네임을 입력해 주세요.',
      ...swalConfig,
    });
    return;
  }

  $.ajax({
    url: '/api/users/check-nickname',
    type: 'GET',
    data: {nickname: nickname},
    dataType: 'json',
    success: function (response) {
      if (!response) {
        Swal.fire({
          icon: 'error',
          text: '서버 응답이 올바르지 않습니다.',
          ...swalConfig,
        });
        return;
      }
      if (response.status === 'error') {
        Swal.fire({
          icon: 'warning',
          text: response.errorMessage,
          ...swalConfig,
        });
      }
      if (response.status === 'success') {
        Swal.fire({
          icon: 'success',
          text: response.data.message,
          showCancelButton: true,
          cancelButtonColor: '#d33',
          confirmButtonText: '사용',
          cancelButtonText: '취소',
          ...swalConfig,
        }).then((result) => {
          if (result.isConfirmed) {
            document
            .getElementById('checkNicknameBtn')
            .setAttribute('data-available', 'true');
            document
            .getElementById('checkNicknameBtn')
            .setAttribute('disabled', true);
          }
        });
      }
    },
    error: function () {
      Swal.fire({
        icon: 'error',
        text: '서버 응답이 올바르지 않습니다.',
        ...swalConfig,
      });
    },
  });
}

// 닉네임 재입력 시 중복 검사 버튼 활성화
document.getElementById('nickname').addEventListener('input', function () {
  if (document.getElementById('checkNicknameBtn').getAttribute('data-available')
      === 'true') {
    document.getElementById('checkNicknameBtn').removeAttribute('disabled');
    document
    .getElementById('checkNicknameBtn')
    .setAttribute('data-available', 'false');
  }
});

document.getElementById('formSubmit').addEventListener('click', checkForm);

function checkForm() {

  if (document.getElementById('checkEmailBtn').getAttribute('data-available')
      === 'false') {
    Swal.fire({
      icon: 'warning',
      text: '이메일 중복 확인을 해주세요.',
      ...swalConfig,
    });
    return false;
  }

  if (document.getElementById('checkNicknameBtn').getAttribute('data-available')
      === 'false') {
    Swal.fire({
      icon: 'warning',
      text: '닉네임 중복 확인을 해주세요.',
      ...swalConfig,
    });
    return false;
  }

  if (document.getElementById('pwd').value !== document.getElementById(
      'pwd2').value) {
    Swal.fire({
      icon: 'warning',
      text: '비밀번호가 일치하지 않습니다.',
      ...swalConfig,
    });
    return false;
  }
  document.getElementById('registerForm').submit();
}