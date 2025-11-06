const urlParams = new URLSearchParams(window.location.search);
function showTab(tabIndex) {
    const allTabs = document.querySelectorAll('.tab-panel');
    allTabs.forEach(tab =>{
        if (tab.id !== "edit-button") tab.style.display = 'none'
    });
    document.getElementsByClassName("tab-panel")[tabIndex].style.display = 'flex';

    const allTabButtons = document.querySelectorAll('.tab-button');
    allTabButtons.forEach(btn =>{
        if (btn.id !== "edit-button") btn.style.borderBottom = '2px solid rgba(0, 0, 0, 0)'
    });
    document.getElementsByClassName("tab-button")[tabIndex].style.borderBottom = '2px solid';
}
showTab(urlParams.get('tab') != null && isInteger(urlParams.get('tab')) ? urlParams.get('tab') : 0);
