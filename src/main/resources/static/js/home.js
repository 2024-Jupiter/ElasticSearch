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
if(userInfo) {

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
      window.open(`/api/users/update`,'_blank',options)


  });
}

const save = document.getElementById('save');
const cancel = document.getElementById('cancel');

if(save) {
  save.addEventListener('click', function () {
    document.getElementById('postForm').submit();
  });
}

if (cancel) {
  cancel.addEventListener('click', function () {
    history.back()
  });
}

const googleLogin = document.getElementById('googleLogin');
const githubLogin = document.getElementById('githubLogin');
const kakaoLogin = document.getElementById('kakaoLogin');

if(kakaoLogin) {
  kakaoLogin.addEventListener('click', function () {
    location.href = 'http://localhost:8070/oauth2/authorization/kakao';
  })
}


if(googleLogin) {
googleLogin.addEventListener('click', function () {
location.href = 'http://localhost:8070/oauth2/authorization/google';
})
}



if(githubLogin) {
githubLogin.addEventListener('click', function () {
location.href = 'http://localhost:8070/oauth2/authorization/github';
})
}
