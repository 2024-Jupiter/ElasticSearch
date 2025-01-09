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

const userInfo = document.getElementById('userInfo');
if (userInfo) {

  document.getElementById('userInfo').addEventListener('click', function () {
    location.href = `/api/users/detail`;
  });

  document.getElementById('logout').addEventListener('click', function () {
    location.href = `/api/users/logout`;
  });
}

const updateBtn = document.getElementById('update');
if (updateBtn) {
  updateBtn.addEventListener('click', function () {
    const options = 'width=700, height=600, top=50, left=50, scrollbars=yes'
    window.open(`/api/users/update`, '_blank', options)

  });
}

const save = document.getElementById('save');
const cancel = document.getElementById('cancel');

if (save) {
  save.addEventListener('click', function () {
    document.getElementById('postForm').submit();
  });
}

if (cancel) {
  cancel.addEventListener('click', function () {
    history.back()
  });
}

const kakaoLogin = document.getElementById('kakaoLogin');

if (kakaoLogin) {
  kakaoLogin.addEventListener('click', function () {
    location.href = 'https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=c597f9b416fd429ed7553dc435b36b4e&scope=profile_nickname%20account_email%20profile_image&state=gS_bYTJC6mOHREv00p1lCuf_0NU0tfbfo0d32IhC5ZE%3D&redirect_uri=http://localhost:8070/login/oauth2/code/kakao&code_challenge=-KAW5YxxqdgclS6bpBiYplEhuTfbmIOek82eMd6T8Bo&code_challenge_method=S256';
  })
}

const githubLogin = document.getElementById('githubLogin');

if (githubLogin) {
  githubLogin.addEventListener('click', function () {
    location.href = 'https://github.com/login/oauth/authorize?response_type=code&client_id=Ov23liOpRJ1khYSHqgQj&scope=user&state=fU4Eg1aLV8XJrRT73FR085vVfPtGEEPtEKhjwZi7oq8%3D&redirect_uri=http://localhost:8070/login/oauth2/code/github';
  })
}

const googleLogin = document.getElementById('googleLogin');

if (googleLogin) {
  googleLogin.addEventListener('click', function () {
    location.href = 'https://github.com/login/oauth/authorize?response_type=code&client_id=Ov23liOpRJ1khYSHqgQj&scope=user&state=fU4Eg1aLV8XJrRT73FR085vVfPtGEEPtEKhjwZi7oq8%3D&redirect_uri=http://localhost:8070/login/oauth2/code/github';
  })
}