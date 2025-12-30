// async function loadItems() {
//     try {
//         const response = await fetch("http://localhost:8080/api/v1/incidents", {
//             method: "GET"
//         });
//         const data = await response.json();
//         console.log(data);

//         const list = document.getElementById("itemList");

//         data.message.forEach(item => {
//             console.log(item);
//             list.insertAdjacentHTML(
//                 "beforeend",
//                 `<li>${item.incidentType}</li>`
//             );
//         });
//     } catch (err) {
//         console.error(err);
//     }
// }

// loadItems();