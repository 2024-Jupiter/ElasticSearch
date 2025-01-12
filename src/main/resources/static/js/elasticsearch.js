// document.addEventListener("DOMContentLoaded", function () {
//   const postId = document.getElementById("postId").value;
//   const recommendationsUrl = `/api/search/posts/recommend/${postId}/similar`;
//
//   fetch(recommendationsUrl)
//   .then(response => {
//     if (!response.ok) {
//       throw new Error(`HTTP error! status: ${response.status}`);
//     }
//     return response.json();
//   })
//   .then(data => {
//     if (data.status === "success") {
//       renderRecommendations(data.data.similarPost);
//     } else {
//       console.error("Unexpected response format:", data);
//     }
//   })
//   .catch(error => {
//     console.error("Error fetching recommendations:", error);
//   });
// });
//
// function renderRecommendations(posts) {
//   let recommendationsContainer = document.getElementById('recommendations-container');
//   let noRecommendationsMessage = document.getElementById('no-recommendations-message');
//
//   // 초기화
//   recommendationsContainer.innerHTML = ''; // 기존 카드 삭제
//
//   let renderedCount = 0; // 렌더링된 카드 수
//
//   for (let i = 0; i < posts.length && renderedCount < 4; i++) {
//     const title = posts[i].title && posts[i].title.trim();
//     const content = posts[i].content;
//
//     if (!title) continue; // 제목이 없는 경우 건너뛰기
//
//     let pTags = content ? content.match(/<p>(.*?)<\/p>/g) : null;
//     let firstPContent = null;
//
//     if (pTags) {
//       for (let pTag of pTags) {
//         let cleanedContent = pTag.replace(
//             /<(img|span|a|strong)[^>]*>.*?<\/\1>|<(img|span|a|strong)[^>]*>/g,
//             ""
//         ).trim();
//         if (cleanedContent.length > 0) {
//           firstPContent = cleanedContent;
//           break;
//         }
//       }
//     }
//
//     if (title && firstPContent) {
//       // 카드 템플릿 복사
//       let cardTemplate = document.getElementById('card-template');
//       let newCard = cardTemplate.cloneNode(true);
//
//       // 카드 데이터 채우기
//       newCard.style.display = 'block';
//       newCard.querySelector('#title-template').innerText = title;
//       newCard.querySelector('#body-template').innerHTML = firstPContent.substring(0, 70);
//
//       // 클릭 이벤트 추가
//       newCard.addEventListener('click', () => {
//         const detailUrl = `/api/search/posts/${posts[i].id}`;
//         window.location.href = detailUrl;
//       });
//
//       // ID 제거 (중복 방지)
//       newCard.removeAttribute('id');
//       recommendationsContainer.appendChild(newCard);
//
//       renderedCount++;
//     }
//   }
//
//   // 유사 게시글 없음 처리
//   if (renderedCount === 0) {
//     noRecommendationsMessage.style.display = 'block';
//   } else {
//     noRecommendationsMessage.style.display = 'none';
//   }
// }

document.addEventListener("DOMContentLoaded", function () {
  const postId = document.getElementById("postId").value;
  const recommendationsUrl = `/api/search/posts/recommend/${postId}/similar`;

  fetch(recommendationsUrl)
  .then((response) => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  })
  .then((data) => {
    if (data.status === "success") {
      renderRecommendations(data.data.similarPost);
    } else {
      console.error("Unexpected response format:", data);
    }
  })
  .catch((error) => {
    console.error("Error fetching recommendations:", error);
  });
});

function renderRecommendations(posts) {
  const recommendationsContainer = document.getElementById("recommendations-container");
  const noRecommendationsMessage = document.getElementById("no-recommendations-message");

  // 초기화
  recommendationsContainer.innerHTML = ""; // 기존 카드 삭제
  noRecommendationsMessage.style.display = "none"; // 메시지 숨기기

  let renderedCount = 0; // 렌더링된 카드 수

  for (let i = 0; i < posts.length; i++) {
    const title = posts[i].title && posts[i].title.trim();
    const content = posts[i].content;

    if (!title) continue; // 제목이 없는 경우 건너뛰기

    const pTags = content ? content.match(/<p>(.*?)<\/p>/g) : null;
    let firstPContent = null;

    if (pTags) {
      for (const pTag of pTags) {
        const cleanedContent = pTag
        .replace(/<(img|span|a|strong)[^>]*>.*?<\/\1>|<(img|span|a|strong)[^>]*>/g, "")
        .trim();
        if (cleanedContent.length > 0) {
          firstPContent = cleanedContent;
          break;
        }
      }
    }

    if (title && firstPContent) {
      // 카드 생성
      const newCard = document.createElement("div");
      newCard.className = "card bg-secondary mb-3";
      newCard.style = "max-width: 19rem; flex: 1; margin: 10px; color: black;";
      newCard.innerHTML = `
        <div class="card-header">${title}</div>
        <div class="card-body">
          <p class="card-text">${firstPContent.substring(0, 70)}</p>
        </div>
      `;

      // 클릭 이벤트 추가
      newCard.addEventListener("click", () => {
        const detailUrl = `/api/search/posts/${posts[i].id}`;
        window.location.href = detailUrl;
      });

      recommendationsContainer.appendChild(newCard);
      renderedCount++;
    }

    // 최대 4개까지만 렌더링
    if (renderedCount >= 4) break;
  }

  // 유사 게시글 없음 처리
  if (renderedCount === 0) {
    noRecommendationsMessage.style.display = "block";
  }
}
