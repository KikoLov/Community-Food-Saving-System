/**
 * SB Admin JavaScript Utilities for Vue 3
 * Adapted from Start Bootstrap SB Admin template
 */

/**
 * Initialize sidebar toggle functionality
 * Call this in layout components on mount
 */
export function initSidebarToggle() {
  // Check localStorage for saved state
  const savedState = localStorage.getItem('sb|sidebar-toggle')
  if (savedState === 'true') {
    document.body.classList.add('sb-sidenav-toggled')
  }

  // Add click listener to sidebar toggle button
  const setupToggle = () => {
    const sidebarToggle = document.body.querySelector('#sidebarToggle')
    if (sidebarToggle) {
      sidebarToggle.addEventListener('click', (event) => {
        event.preventDefault()
        document.body.classList.toggle('sb-sidenav-toggled')
        localStorage.setItem('sb|sidebar-toggle', document.body.classList.contains('sb-sidenav-toggled'))
      })
    }
  }

  // Use setTimeout to ensure DOM is ready
  setTimeout(setupToggle, 0)
}

/**
 * Initialize DataTables for a given table element
 * @param {string} tableId - ID of the table element
 */
export function initDataTable(tableId) {
  import('simple-datatables').then(({ default: simpleDatatables }) => {
    const table = document.getElementById(tableId)
    if (table) {
      new simpleDatatables.DataTable(table, {
        searchable: true,
        sortable: true,
        fixedHeight: true,
        perPage: 10,
        perPageSelect: [5, 10, 25, 50]
      })
    }
  })
}

/**
 * Close all dropdown menus
 */
export function closeAllDropdowns() {
  const dropdowns = document.querySelectorAll('.dropdown-menu.show')
  dropdowns.forEach(dropdown => {
    dropdown.classList.remove('show')
  })
}
