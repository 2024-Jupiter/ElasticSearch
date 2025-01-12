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



document.addEventListener("DOMContentLoaded", function () {
  let user = document.getElementById('esLoad').value

  if (user == 'Guest') {
    loadPosts(2);
  } else {
    loadPosts(4);
  }

  function loadPosts(page) {


    $.ajax({
      url: "/api/search/posts/recent", // Spring 매핑 경로와 동일하게 설정
      type: "GET",
      data: {p: page},
      success: function (response) {
        console.log(response)
        console.log(response.data)
        if (response.status == 'success' && response.data.postList) {
          renderPosts(response.data.postList);
        } else {
          console.error("Invalid response structure:", response.data);
        }
      },
      error: function (xhr, status, error) {
        console.error("Error loading posts:", error);
      },
    });

    function renderPosts(postList) {
      const postListElement = document.getElementById("post-list");
      postListElement.innerHTML = ""; // Clear existing content
      postList.forEach(post => {
        const createdAt = new Date(post.createdAt);
        const row = `
                <tr>
                    <td style="width: 55%; text-align: left">
                        <a href="/api/search/posts/${post.id}">${post.title}</a>
                    </td>
                    <td style="width: 10%;">${post.author || "익명"}</td>
                    <td style="width: 10%;">

            ${createdAt.toLocaleDateString()}<br>
              ${createdAt.toLocaleTimeString()}
</td>
                </tr>`;
        postListElement.insertAdjacentHTML("beforeend", row);
      });
    }

    function renderPagination(pageList, currentPage, totalPages) {
      const paginationElement = document.getElementById("pagination");
      paginationElement.innerHTML = ""; // Clear existing content

      pageList.forEach(page => {
        const activeClass = page === currentPage ? "active" : "";
        const pageItem = `
                <li class="page-item ${activeClass}">
                    <a class="page-link" href="#" data-page="${page}">${page}</a>
                </li>`;
        paginationElement.insertAdjacentHTML("beforeend", pageItem);
      });

      // Add click event to pagination links
      paginationElement.querySelectorAll(".page-link").forEach(link => {
        link.addEventListener("click", function (e) {
          e.preventDefault();
          const selectedPage = parseInt(this.getAttribute("data-page"));
          loadPosts(selectedPage);
        });
      });
    }
  }
})


