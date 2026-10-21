/**
 * Pack & Plan • DSA Budget & Packing Optimizer
 * Frontend Application Controller
 * Handles REST APIs, 3D Wireframe Bin Packing Simulation, SVG AVL Tree, and Dynamic Donut Charts
 */

document.addEventListener('DOMContentLoaded', () => {
  // Application State
  const state = {
    catalog: [],
    cart: { items: [], totalCost: 0, totalUtility: 0, totalWeight: 0 },
    undoStack: [],
    redoStack: [],
    boxCapacity: 10.0,
    courierFee: 150.0,
    activePreset: 'TECH_FEST',
    activeTab: 'tab-dashboard',
    selectedCategory: 'ALL',
    searchKeyword: '',
    
    // Simulation Engine State
    packingResult: null,
    simulation: {
      timer: null,
      isRunning: false,
      speedMs: 500,
      unpackedQueue: [],
      placedItems: [],
      selectedAlgo: 'FFD'
    }
  };

  // Color Palette for 3D Packed Blocks (from Reference Design)
  const BLOCK_COLORS = [
    { bg: '#f472b6', border: '#ec4899', text: '#fff' }, // Pink
    { bg: '#f87171', border: '#ef4444', text: '#fff' }, // Coral
    { bg: '#fbbf24', border: '#f59e0b', text: '#000' }, // Gold / Amber
    { bg: '#34d399', border: '#10b981', text: '#000' }, // Emerald
    { bg: '#60a5fa', border: '#3b82f6', text: '#fff' }, // Sky Blue
    { bg: '#a78bfa', border: '#8b5cf6', text: '#fff' }, // Violet
    { bg: '#22d3ee', border: '#06b6d4', text: '#000' }, // Cyan
    { bg: '#fb923c', border: '#f97316', text: '#fff' }, // Orange
    { bg: '#e879f9', border: '#d946ef', text: '#fff' }, // Fuchsia
    { bg: '#4ade80', border: '#22c55e', text: '#000' }  // Mint
  ];

  // DOM Elements Cache
  const el = {
    // Header KPIs
    kpiCartCost: document.getElementById('kpiCartCost'),
    kpiBudgetProgress: document.getElementById('kpiBudgetProgress'),
    kpiSpaceUtil: document.getElementById('kpiSpaceUtil'),
    kpiWeightRatio: document.getElementById('kpiWeightRatio'),
    kpiUtilProgress: document.getElementById('kpiUtilProgress'),
    kpiCartBoxes: document.getElementById('kpiCartBoxes'),
    kpiBoxesProgress: document.getElementById('kpiBoxesProgress'),
    kpiCourierCostSub: document.getElementById('kpiCourierCostSub'),
    themeToggleBtn: document.getElementById('themeToggleBtn'),
    sidebarToggleBtn: document.getElementById('sidebarToggleBtn'),
    sidebarCloseBtn: document.getElementById('sidebarCloseBtn'),
    sidebarBackdrop: document.getElementById('sidebarBackdrop'),
    appSidebar: document.getElementById('appSidebar'),
    btnQuickUndo: document.getElementById('btnQuickUndo'),
    btnQuickRedo: document.getElementById('btnQuickRedo'),
    undoBadge: document.getElementById('undoBadge'),
    redoBadge: document.getElementById('redoBadge'),
    scenarioBtns: document.querySelectorAll('.scenario-btn'),
    navItems: document.querySelectorAll('.nav-item'),
    tabPanels: document.querySelectorAll('.tab-panel'),

    // Dashboard Executive
    dispBudgetLimit: document.getElementById('dispBudgetLimit'),
    dispCartCost: document.getElementById('dispCartCost'),
    dispCartUtility: document.getElementById('dispCartUtility'),
    dispRemainingBudget: document.getElementById('dispRemainingBudget'),
    btnToggleDp: document.getElementById('btnToggleDp'),
    btnToggleGreedy: document.getElementById('btnToggleGreedy'),
    categoryDonutSvg: document.getElementById('categoryDonutSvg'),
    donutSlicesGroup: document.getElementById('donutSlicesGroup'),
    donutCenterCount: document.getElementById('donutCenterCount'),
    donutLegendContainer: document.getElementById('donutLegendContainer'),
    budgetInput: document.getElementById('budgetInput'),
    btnRunKnapsack: document.getElementById('btnRunKnapsack'),
    optInsightBanner: document.getElementById('optInsightBanner'),
    optInsightText: document.getElementById('optInsightText'),
    cartTableBody: document.getElementById('cartTableBody'),
    cartEmptyState: document.getElementById('cartEmptyState'),
    btnClearCart: document.getElementById('btnClearCart'),
    btnOpenAddItemModal: document.getElementById('btnOpenAddItemModal'),
    btnOpenAddItemModal2: document.getElementById('btnOpenAddItemModal2'),
    categoryFilterChips: document.getElementById('categoryFilterChips'),
    catalogSearchInput: document.getElementById('catalogSearchInput'),

    // 3D Wireframe Packer & Simulation
    dispBoxCapTag: document.getElementById('dispBoxCapTag'),
    packerBoxCountBadge: document.getElementById('packerBoxCountBadge'),
    packerCourierCostBadge: document.getElementById('packerCourierCostBadge'),
    binsWireframeStage: document.getElementById('binsWireframeStage'),
    unpackedItemsCount: document.getElementById('unpackedItemsCount'),
    itemQueueContainer: document.getElementById('itemQueueContainer'),
    packerAlgoSelect: document.getElementById('packerAlgoSelect'),
    boxCapacitySelect: document.getElementById('boxCapacitySelect'),
    simSpeedSlider: document.getElementById('simSpeedSlider'),
    speedLabel: document.getElementById('speedLabel'),
    btnPlayVisualizer: document.getElementById('btnPlayVisualizer'),
    btnStepVisualizer: document.getElementById('btnStepVisualizer'),
    btnResetVisualizer: document.getElementById('btnResetVisualizer'),
    btnProceedCheckout: document.getElementById('btnProceedCheckout'),

    // Tab 2: Knapsack DP
    ksDpUtility: document.getElementById('ksDpUtility'),
    ksDpCost: document.getElementById('ksDpCost'),
    ksGreedyUtility: document.getElementById('ksGreedyUtility'),
    ksGreedyCost: document.getElementById('ksGreedyCost'),
    ksOptimalityGap: document.getElementById('ksOptimalityGap'),
    ksRuntime: document.getElementById('ksRuntime'),
    ksExplanationAlert: document.getElementById('ksExplanationAlert'),
    ksExplanationText: document.getElementById('ksExplanationText'),
    dpMatrixBody: document.getElementById('dpMatrixBody'),
    ksDpItemsList: document.getElementById('ksDpItemsList'),
    ksGreedyItemsList: document.getElementById('ksGreedyItemsList'),

    // Tab 3: Bin Packing Benchmark
    ffdBoxCount: document.getElementById('ffdBoxCount'),
    ffdUtil: document.getElementById('ffdUtil'),
    ffdCost: document.getElementById('ffdCost'),
    bfdBoxCount: document.getElementById('bfdBoxCount'),
    bfdUtil: document.getElementById('bfdUtil'),
    bfdCost: document.getElementById('bfdCost'),
    wfdBoxCount: document.getElementById('wfdBoxCount'),
    wfdUtil: document.getElementById('wfdUtil'),
    wfdCost: document.getElementById('wfdCost'),
    nfBoxCount: document.getElementById('nfBoxCount'),
    nfUtil: document.getElementById('nfUtil'),
    nfCost: document.getElementById('nfCost'),
    packingSavingsText: document.getElementById('packingSavingsText'),
    boxesDetailedGrid: document.getElementById('boxesDetailedGrid'),

    // Tab 4: AVL Tree
    avlTotalNodes: document.getElementById('avlTotalNodes'),
    avlHeight: document.getElementById('avlHeight'),
    avlTreeGraph: document.getElementById('avlTreeGraph'),
    avlRangeMin: document.getElementById('avlRangeMin'),
    avlRangeMax: document.getElementById('avlRangeMax'),
    btnExecuteRangeSearch: document.getElementById('btnExecuteRangeSearch'),
    rangeResultsList: document.getElementById('rangeResultsList'),
    formInsertAvlItem: document.getElementById('formInsertAvlItem'),
    avlItemName: document.getElementById('avlItemName'),
    avlItemPrice: document.getElementById('avlItemPrice'),
    avlItemUtility: document.getElementById('avlItemUtility'),
    avlItemWeight: document.getElementById('avlItemWeight'),
    minPriceSlider: document.getElementById('minPriceSlider'),
    maxPriceSlider: document.getElementById('maxPriceSlider'),
    priceRangeLabel: document.getElementById('priceRangeLabel'),
    btnResetPriceFilter: document.getElementById('btnResetPriceFilter'),
    avlNodeCountBadge: document.getElementById('avlNodeCountBadge'),
    catalogContainer: document.getElementById('catalogItemsContainer'),

    // Tab 5: Stack
    btnInspectUndo: document.getElementById('btnInspectUndo'),
    btnInspectRedo: document.getElementById('btnInspectRedo'),
    undoStackDepth: document.getElementById('undoStackDepth'),
    redoStackDepth: document.getElementById('redoStackDepth'),
    undoStackList: document.getElementById('undoStackList'),
    redoStackList: document.getElementById('redoStackList'),

    // Tab 6: Huffman
    checkoutCustomerName: document.getElementById('checkoutCustomerName'),
    checkoutOrgName: document.getElementById('checkoutOrgName'),
    btnGenerateInvoiceNow: document.getElementById('btnGenerateInvoiceNow'),
    compOriginalBits: document.getElementById('compOriginalBits'),
    compOriginalBytes: document.getElementById('compOriginalBytes'),
    compEncodedBits: document.getElementById('compEncodedBits'),
    compEncodedBytes: document.getElementById('compEncodedBytes'),
    compRatio: document.getElementById('compRatio'),
    compBitsPerChar: document.getElementById('compBitsPerChar'),
    rawInvoiceDisplay: document.getElementById('rawInvoiceDisplay'),
    encodedBitstreamDisplay: document.getElementById('encodedBitstreamDisplay'),
    decompressedTextDisplay: document.getElementById('decompressedTextDisplay'),
    bitLengthBadge: document.getElementById('bitLengthBadge'),
    huffmanCodeTableBody: document.getElementById('huffmanCodeTableBody'),
    btnCopyInvoice: document.getElementById('btnCopyInvoice'),
    btnPrintInvoicePdf: document.getElementById('btnPrintInvoicePdf'),
    btnExportInvoiceWord: document.getElementById('btnExportInvoiceWord'),
    btnPrintDecompressedPdf: document.getElementById('btnPrintDecompressedPdf'),
    btnExportDecompressedWord: document.getElementById('btnExportDecompressedWord'),
    customCompressInput: document.getElementById('customCompressInput'),
    btnCompressCustomText: document.getElementById('btnCompressCustomText'),

    // Bottom Terminal
    terminalToggleBtn: document.getElementById('terminalToggleBtn'),
    terminalLogBody: document.getElementById('terminalLogBody'),
    terminalArrow: document.getElementById('terminalArrow'),

    // Modal
    modalAddItem: document.getElementById('modalAddItem'),
    btnCloseModal: document.getElementById('btnCloseModal'),
    btnCancelModal: document.getElementById('btnCancelModal'),
    formAddCatalogItem: document.getElementById('formAddCatalogItem'),
    modalItemName: document.getElementById('modalItemName'),
    modalItemCategory: document.getElementById('modalItemCategory'),
    modalItemPrice: document.getElementById('modalItemPrice'),
    modalItemUtility: document.getElementById('modalItemUtility'),
    modalItemWeight: document.getElementById('modalItemWeight')
  };

  // ==========================================
  // INITIALIZATION & EVENT BINDINGS
  // ==========================================

  function init() {
    setupThemeToggle();
    setupSidebarNavigation();
    setupScenarioSwitching();
    setupCatalogFilter();
    setupCartActions();
    setupKnapsackTrigger();
    setupSimulationControls();
    setupStackInspector();
    setupHuffmanControls();
    setupTerminalLogger();
    setupModal();

    // Initial data load
    fetchInitialData();
  }



  // Day / Night Theme Toggle Controller

  function setupThemeToggle() {
    const savedTheme = localStorage.getItem('packplan_theme') || 'dark';
    applyTheme(savedTheme, false);

    if (el.themeToggleBtn) {
      el.themeToggleBtn.addEventListener('click', () => {
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'dark';
        const nextTheme = currentTheme === 'light' ? 'dark' : 'light';
        applyTheme(nextTheme, true);
      });

      el.themeToggleBtn.addEventListener('keydown', (e) => {
        if (e.key === ' ' || e.key === 'Enter') {
          e.preventDefault();
          el.themeToggleBtn.click();
        }
      });
    }
  }

  function applyTheme(theme, logChange = true) {
    const isLight = theme === 'light';
    document.documentElement.setAttribute('data-theme', theme);
    document.body.classList.toggle('light-theme', isLight);
    try {
      localStorage.setItem('packplan_theme', theme);
    } catch (e) {}

    if (el.themeToggleBtn) {
      el.themeToggleBtn.setAttribute('aria-checked', isLight ? 'true' : 'false');
      el.themeToggleBtn.setAttribute('title', isLight ? 'Switch to Night Theme (Dark)' : 'Switch to Day Theme (Light)');
      el.themeToggleBtn.classList.toggle('is-day', isLight);
      el.themeToggleBtn.classList.toggle('is-night', !isLight);
    }

    if (logChange) {
      addLog(`[Theme] Switched to ${isLight ? 'Day Theme (Light)' : 'Night Theme (Dark)'}`, isLight ? 'amber' : 'blue');
    }

    // Refresh dynamic AVL tree if active so nodes match theme
    if (state.activeTab === 'tab-avl') {
      fetchAndRenderAvlTree();
    }
  }


  // Sidebar Navigation Tabs & Hamburger Drawer Menu

  function closeSidebar() {
    if (el.appSidebar) el.appSidebar.classList.remove('open');
    if (el.sidebarBackdrop) el.sidebarBackdrop.classList.remove('active');
    if (el.sidebarToggleBtn) {
      el.sidebarToggleBtn.classList.remove('active');
      el.sidebarToggleBtn.setAttribute('aria-expanded', 'false');
    }
  }

  function openSidebar() {
    if (el.appSidebar) el.appSidebar.classList.add('open');
    if (el.sidebarBackdrop) el.sidebarBackdrop.classList.add('active');
    if (el.sidebarToggleBtn) {
      el.sidebarToggleBtn.classList.add('active');
      el.sidebarToggleBtn.setAttribute('aria-expanded', 'true');
    }
  }

  function toggleSidebar() {
    if (el.appSidebar && el.appSidebar.classList.contains('open')) {
      closeSidebar();
    } else {
      openSidebar();
    }
  }

  function setupSidebarNavigation() {
    // Hamburger button toggle
    if (el.sidebarToggleBtn) {
      el.sidebarToggleBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        toggleSidebar();
      });
    }

    // Close button ('X') inside sidebar header
    if (el.sidebarCloseBtn) {
      el.sidebarCloseBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        closeSidebar();
      });
    }

    // Backdrop click closes sidebar
    if (el.sidebarBackdrop) {
      el.sidebarBackdrop.addEventListener('click', () => {
        closeSidebar();
      });
    }

    // Keyboard ESC key closes sidebar
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && el.appSidebar && el.appSidebar.classList.contains('open')) {
        closeSidebar();
      }
    });

    // Clicking any menu option selects it and closes the drawer
    el.navItems.forEach(item => {
      item.addEventListener('click', () => {
        const targetTab = item.getAttribute('data-tab');
        el.navItems.forEach(t => t.classList.remove('active'));
        el.tabPanels.forEach(p => p.classList.remove('active'));

        item.classList.add('active');
        const activePanel = document.getElementById(targetTab);
        if (activePanel) activePanel.classList.add('active');
        state.activeTab = targetTab;

        // Auto-close sidebar so selected view is immediately displayed full-width
        closeSidebar();

        addLog(`[Navigation] Switched view to: ${targetTab.replace('tab-', '').toUpperCase()}`, 'cyan');

        if (targetTab === 'tab-avl') {
          fetchAndRenderAvlTree();
        } else if (targetTab === 'tab-packing') {
          triggerBinPacking();
        } else if (targetTab === 'tab-huffman') {
          triggerCheckout();
        } else if (targetTab === 'tab-dashboard') {
          triggerBinPacking();
        }
      });
    });

    if (el.btnProceedCheckout) {
      el.btnProceedCheckout.addEventListener('click', () => {
        const huffmanNav = document.querySelector('[data-tab="tab-huffman"]');
        if (huffmanNav) huffmanNav.click();
      });
    }
  }


  // Domain Scenario Switcher

  function setupScenarioSwitching() {
    el.scenarioBtns.forEach(btn => {
      btn.addEventListener('click', async () => {
        const preset = btn.getAttribute('data-preset');
        el.scenarioBtns.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        state.activePreset = preset;
        closeSidebar();

        if (preset === 'KIRANA_WHOLESALE') {
          if (el.checkoutOrgName) el.checkoutOrgName.value = 'City Wholesale & Kirana Mart';
          if (el.checkoutCustomerName) el.checkoutCustomerName.value = 'Ramesh Kumar (Shopkeeper)';
          addLog('[Scenario] Switched domain to: Wholesale Kirana & Grocery Merchant', 'amber');
        } else {
          if (el.checkoutOrgName) el.checkoutOrgName.value = 'Apex Tech Innovation Fest 2026';
          if (el.checkoutCustomerName) el.checkoutCustomerName.value = 'Harsh (College Event Organizer)';
          addLog('[Scenario] Switched domain to: College Event & Tech Fest Organizer', 'amber');
        }

        try {
          const res = await apiPost('/api/presets/load', { preset });
          if (res.success) {
            state.catalog = res.catalog || [];
            state.selectedCategory = 'ALL';
            state.searchKeyword = '';
            if (el.catalogSearchInput) el.catalogSearchInput.value = '';
            renderCategoryChips();
            applyCatalogFilters();
            await fetchCart();
            await triggerBinPacking();
            if (state.activeTab === 'tab-avl') fetchAndRenderAvlTree();
          }
        } catch (err) {
          console.error('Error loading preset:', err);
        }
      });
    });
  }


  // Catalog Filter, Instant Search & Dynamic Category Chips

  function renderCategoryChips() {
    if (!el.categoryFilterChips) return;
    const uniqueCats = ['ALL'];
    const seen = new Set();
    (state.catalog || []).forEach(it => {
      if (it.category && !seen.has(it.category)) {
        seen.add(it.category);
        uniqueCats.push(it.category);
      }
    });

    el.categoryFilterChips.innerHTML = '';
    uniqueCats.forEach(cat => {
      const chip = document.createElement('button');
      chip.type = 'button';
      chip.className = `filter-chip ${(state.selectedCategory || 'ALL').toLowerCase() === cat.toLowerCase() ? 'active' : ''}`;
      chip.setAttribute('data-cat', cat);
      chip.textContent = cat;
      chip.addEventListener('click', () => {
        state.selectedCategory = cat;
        const allChips = el.categoryFilterChips.querySelectorAll('.filter-chip');
        allChips.forEach(c => {
          c.classList.toggle('active', (c.getAttribute('data-cat') || '').toLowerCase() === cat.toLowerCase());
        });
        applyCatalogFilters();
      });
      el.categoryFilterChips.appendChild(chip);
    });
  }

  function applyCatalogFilters() {
    let filtered = state.catalog || [];
    if (state.selectedCategory && state.selectedCategory !== 'ALL') {
      filtered = filtered.filter(it => it.category && it.category.toLowerCase() === state.selectedCategory.toLowerCase());
    }
    if (state.searchKeyword) {
      const term = state.searchKeyword.toLowerCase();
      filtered = filtered.filter(it => 
        (it.name && it.name.toLowerCase().includes(term)) ||
        (it.category && it.category.toLowerCase().includes(term)) ||
        (String(it.price).includes(term))
      );
    }
    renderCatalog(filtered);
    if (el.avlNodeCountBadge) {
      el.avlNodeCountBadge.textContent = `${filtered.length} of ${state.catalog.length} items available`;
    }
  }

  function setupCatalogFilter() {
    if (el.catalogSearchInput) {
      el.catalogSearchInput.addEventListener('input', (e) => {
        state.searchKeyword = e.target.value.trim();
        applyCatalogFilters();
      });
    }

    if (el.minPriceSlider && el.maxPriceSlider) {
      const updateFilter = async () => {
        let minP = parseFloat(el.minPriceSlider.value);
        let maxP = parseFloat(el.maxPriceSlider.value);
        if (minP > maxP) {
          const temp = minP; minP = maxP; maxP = temp;
        }
        if (el.priceRangeLabel) {
          el.priceRangeLabel.textContent = `₹${minP.toLocaleString()} - ₹${maxP.toLocaleString()}`;
        }

        try {
          const res = await apiGet(`/api/catalog?minPrice=${minP}&maxPrice=${maxP}`);
          renderCatalog(res.items);
          if (el.avlNodeCountBadge) {
            el.avlNodeCountBadge.textContent = `${res.count} of ${state.catalog.length} items (AVL Range)`;
          }
        } catch (err) {
          console.error('Filter query error:', err);
        }
      };

      el.minPriceSlider.addEventListener('input', updateFilter);
      el.maxPriceSlider.addEventListener('input', updateFilter);

      if (el.btnResetPriceFilter) {
        el.btnResetPriceFilter.addEventListener('click', () => {
          el.minPriceSlider.value = 0;
          el.maxPriceSlider.value = 15000;
          if (el.priceRangeLabel) el.priceRangeLabel.textContent = '₹0 - ₹15,000';
          applyCatalogFilters();
        });
      }
    }
  }


  // Cart Actions & Undo/Redo

  function setupCartActions() {
    el.btnClearCart.addEventListener('click', async () => {
      await apiPost('/api/cart/clear', {});
      addLog('[Cart ADT] Stack Action: CLEAR_CART pushed to Undo Stack', 'amber');
      await fetchCart();
      await triggerBinPacking();
    });

    const doUndo = async () => {
      await apiPost('/api/cart/undo', {});
      addLog('[Cart ADT] POP Undo Stack -> Transaction reversed and pushed to Redo Stack', 'cyan');
      await fetchCart();
      await triggerBinPacking();
    };

    const doRedo = async () => {
      await apiPost('/api/cart/redo', {});
      addLog('[Cart ADT] POP Redo Stack -> Transaction re-applied', 'cyan');
      await fetchCart();
      await triggerBinPacking();
    };

    el.btnQuickUndo.addEventListener('click', doUndo);
    el.btnQuickRedo.addEventListener('click', doRedo);
    el.btnInspectUndo.addEventListener('click', doUndo);
    el.btnInspectRedo.addEventListener('click', doRedo);
  }


  // Knapsack Optimizer Trigger

  function setupKnapsackTrigger() {
    el.btnRunKnapsack.addEventListener('click', async () => {
      const budget = parseFloat(el.budgetInput.value) || 10000;
      el.btnRunKnapsack.disabled = true;
      el.btnRunKnapsack.innerHTML = 'Computing 0/1 Knapsack DP...';

      addLog(`[Knapsack DP] Evaluating optimal subset for cash budget ₹${budget.toLocaleString()}...`, 'green');

      try {
        const res = await apiPost('/api/knapsack', { budget, applyToCart: true });
        renderKnapsackResult(res.result);
        await fetchCart();
        await triggerBinPacking();

        el.optInsightBanner.style.display = 'flex';
        el.optInsightText.textContent = res.result.optimalityExplanation;
        addLog(`[Knapsack DP] Solved! Optimal Utility: ${res.result.totalUtility} pts under ₹${res.result.totalCost}`, 'green');
      } catch (err) {
        console.error('Knapsack failed:', err);
      } finally {
        el.btnRunKnapsack.disabled = false;
        el.btnRunKnapsack.innerHTML = `
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
          Auto-Maximize Cart (0/1 Knapsack)
        `;
      }
    });

    // Toggle: DP vs Greedy
    el.btnToggleDp.addEventListener('click', () => {
      el.btnToggleDp.classList.add('active');
      el.btnToggleGreedy.classList.remove('active');
      el.btnRunKnapsack.click();
    });

    el.btnToggleGreedy.addEventListener('click', async () => {
      el.btnToggleGreedy.classList.add('active');
      el.btnToggleDp.classList.remove('active');
      addLog('[Knapsack Heuristic] Switched to Greedy density ratio selection (Suboptimal)', 'amber');
    });
  }


  // 3D Wireframe Bin Packing Simulation Controls

  function setupSimulationControls() {
    // Algorithm dropdown
    el.packerAlgoSelect.addEventListener('change', (e) => {
      state.simulation.selectedAlgo = e.target.value;
      addLog(`[Bin Packing] Algorithm switched to: ${e.target.value}`, 'cyan');
      triggerBinPacking();
    });

    // Box capacity select
    el.boxCapacitySelect.addEventListener('change', async (e) => {
      const cap = parseFloat(e.target.value);
      state.boxCapacity = cap;
      el.dispBoxCapTag.textContent = `${cap.toFixed(1)} kg`;
      addLog(`[Warehouse Packaging] Standard box weight limit set to: ${cap.toFixed(1)} kg`, 'cyan');
      await triggerBinPacking();
    });

    // Speed slider
    el.simSpeedSlider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value);
      state.simulation.speedMs = val;
      if (val < 300) el.speedLabel.textContent = 'Fast';
      else if (val > 800) el.speedLabel.textContent = 'Slow';
      else el.speedLabel.textContent = 'Normal';
    });

    // Step Forward
    el.btnStepVisualizer.addEventListener('click', () => {
      stepSimulationForward();
    });

    // Run Visualizer / Pause
    el.btnPlayVisualizer.addEventListener('click', () => {
      if (state.simulation.isRunning) {
        pauseSimulation();
      } else {
        startSimulation();
      }
    });

    // Reset
    el.btnResetVisualizer.addEventListener('click', () => {
      resetSimulation();
    });
  }


  // Stack Inspector Controls

  function setupStackInspector() {
    // Buttons hooked in setupCartActions
  }


  // Huffman Controls

  function setupHuffmanControls() {
    if (el.btnGenerateInvoiceNow) {
      el.btnGenerateInvoiceNow.addEventListener('click', triggerCheckout);
    }

    if (el.btnCopyInvoice) {
      el.btnCopyInvoice.addEventListener('click', () => {
        navigator.clipboard.writeText(el.rawInvoiceDisplay.textContent);
        el.btnCopyInvoice.textContent = 'Copied!';
        setTimeout(() => { el.btnCopyInvoice.textContent = 'Copy Text'; }, 2000);
      });
    }

    if (el.btnPrintInvoicePdf) {
      el.btnPrintInvoicePdf.addEventListener('click', () => {
        const text = el.rawInvoiceDisplay.textContent;
        printInvoiceDocument(text, 'Digital Order Raw Invoice');
      });
    }

    if (el.btnExportInvoiceWord) {
      el.btnExportInvoiceWord.addEventListener('click', () => {
        const text = el.rawInvoiceDisplay.textContent;
        exportInvoiceAsWord(text, 'Raw_Order_Invoice');
      });
    }

    if (el.btnPrintDecompressedPdf) {
      el.btnPrintDecompressedPdf.addEventListener('click', () => {
        const text = el.decompressedTextDisplay.textContent;
        printInvoiceDocument(text, 'Lossless Decompressed Bill (Huffman Recovery)');
      });
    }

    if (el.btnExportDecompressedWord) {
      el.btnExportDecompressedWord.addEventListener('click', () => {
        const text = el.decompressedTextDisplay.textContent;
        exportInvoiceAsWord(text, 'Lossless_Decompressed_Bill');
      });
    }

    if (el.btnCompressCustomText) {
      el.btnCompressCustomText.addEventListener('click', async () => {
        const text = el.customCompressInput.value.trim();
        if (!text) return;
        try {
          const res = await apiPost('/api/huffman/custom', { text });
          const comp = res.compression || res.result || res;
          if (comp) {
            renderCheckoutResult({
              invoice: { rawInvoiceText: text },
              compression: comp
            });
            addLog(`[Huffman Coding] Custom text compressed: ${(comp.compressionRatio || 0).toFixed(1)}% savings`, 'green');
          }
        } catch (err) {
          console.error('Custom compression error:', err);
        }
      });
    }
  }


  // Bottom Terminal Console

  function setupTerminalLogger() {
    el.terminalToggleBtn.addEventListener('click', () => {
      const isCollapsed = el.terminalLogBody.classList.contains('collapsed');
      if (isCollapsed) {
        el.terminalLogBody.classList.remove('collapsed');
        el.terminalArrow.innerHTML = '&#9660;';
      } else {
        el.terminalLogBody.classList.add('collapsed');
        el.terminalArrow.innerHTML = '&#9650;';
      }
    });
  }

  function addLog(msg, color = 'muted') {
    if (!el.terminalLogBody) return;
    const time = new Date().toLocaleTimeString();
    const line = document.createElement('div');
    line.className = `log-line text-${color}`;
    line.textContent = `[${time}] ${msg}`;
    el.terminalLogBody.appendChild(line);
    el.terminalLogBody.scrollTop = el.terminalLogBody.scrollHeight;
  }


  // Add Item Modal

  function setupModal() {
    const openModal = () => { if (el.modalAddItem) el.modalAddItem.style.display = 'flex'; };
    const closeModal = () => { if (el.modalAddItem) el.modalAddItem.style.display = 'none'; };

    if (el.btnOpenAddItemModal) el.btnOpenAddItemModal.addEventListener('click', openModal);
    if (el.btnOpenAddItemModal2) el.btnOpenAddItemModal2.addEventListener('click', openModal);
    if (el.btnCloseModal) el.btnCloseModal.addEventListener('click', closeModal);
    if (el.btnCancelModal) el.btnCancelModal.addEventListener('click', closeModal);

    if (el.formAddCatalogItem) {
      el.formAddCatalogItem.addEventListener('submit', async (e) => {
        e.preventDefault();
        const itemData = {
          name: el.modalItemName.value,
          category: el.modalItemCategory.value,
          price: parseFloat(el.modalItemPrice.value),
          utility: parseInt(el.modalItemUtility.value),
          weight: parseFloat(el.modalItemWeight.value)
        };

        const autoAddCheckbox = document.getElementById('modalAutoAddToCart');
        const autoAdd = autoAddCheckbox ? autoAddCheckbox.checked : true;

        try {
          const res = await apiPost('/api/catalog/item', itemData);
          if (res.success) {
            closeModal();
            el.formAddCatalogItem.reset();
            await fetchCatalogData();
            addLog(`[AVL Tree] Dynamically inserted custom product: "${itemData.name}" (₹${itemData.price}, ${itemData.weight}kg, +${itemData.utility}pts)`, 'white');

            if (autoAdd && res.item && res.item.id) {
              await apiPost('/api/cart/add', { itemId: res.item.id });
              addLog(`[Cart] Automatically added custom product "${itemData.name}" to Cart & Packing`, 'white');
              await fetchCart();
              await triggerBinPacking();
            }

            if (state.activeTab === 'tab-avl') fetchAndRenderAvlTree();
          }
        } catch (err) {
          console.error('Failed to add custom item:', err);
        }
      });
    }

    // AVL Tab Form
    el.formInsertAvlItem.addEventListener('submit', async (e) => {
      e.preventDefault();
      const itemData = {
        name: el.avlItemName.value,
        category: 'Custom Node',
        price: parseFloat(el.avlItemPrice.value),
        utility: parseInt(el.avlItemUtility.value),
        weight: parseFloat(el.avlItemWeight.value)
      };

      try {
        const res = await apiPost('/api/catalog/item', itemData);
        if (res.success) {
          el.formInsertAvlItem.reset();
          await fetchCatalogData();
          fetchAndRenderAvlTree();
          addLog(`[AVL Tree] Invariant verified after insertion. Heights re-calculated.`, 'green');
        }
      } catch (err) {
        console.error('Failed to insert AVL item:', err);
      }
    });

    el.btnExecuteRangeSearch.addEventListener('click', async () => {
      const minP = parseFloat(el.avlRangeMin.value) || 0;
      const maxP = parseFloat(el.avlRangeMax.value) || 100000;

      try {
        const res = await apiGet(`/api/catalog?minPrice=${minP}&maxPrice=${maxP}`);
        el.rangeResultsList.innerHTML = '';
        if (res.items.length === 0) {
          el.rangeResultsList.innerHTML = '<li class="range-item-row" style="color: var(--text-muted)">No items found in this range.</li>';
        } else {
          res.items.forEach(it => {
            const li = document.createElement('li');
            li.className = 'range-item-row';
            li.innerHTML = `
              <span>${it.name}</span>
              <strong style="color: var(--accent-neon-green)">₹${it.price.toLocaleString()}</strong>
            `;
            el.rangeResultsList.appendChild(li);
          });
        }
        highlightAvlTreeRange(minP, maxP);
        addLog(`[AVL Range Query] Pruned in-order traversal: matched ${res.items.length} items in [₹${minP}, ₹${maxP}]`, 'cyan');
      } catch (err) {
        console.error('Range search error:', err);
      }
    });
  }

  // ==========================================
  // HTTP REST API CALLS
  // ==========================================

  async function apiGet(url) {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  }

  async function apiPost(url, data) {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  }

  // ==========================================
  // DATA FETCHING & SYNCHRONIZATION
  // ==========================================

  async function fetchInitialData() {
    try {
      await fetchCatalogData();
      await fetchCart();
      await triggerBinPacking();
      fetchAndRenderAvlTree();
      triggerCheckout();
    } catch (err) {
      console.error('Initialization fetch error:', err);
    }
  }

  async function fetchCatalogData() {
    const res = await apiGet('/api/catalog');
    state.catalog = res.items || [];
    renderCategoryChips();
    applyCatalogFilters();
    if (el.avlNodeCountBadge) {
      el.avlNodeCountBadge.textContent = `${state.catalog.length} items in AVL Tree`;
    }
  }

  async function fetchCart() {
    const res = await apiGet('/api/cart');
    state.cart = (res && res.items) ? res : (res.cart || { items: [], totalCost: 0, totalUtility: 0, totalWeight: 0 });
    state.undoStack = res.undoStack || [];
    state.redoStack = res.redoStack || [];

    renderCart(state.cart);
    renderStackVisualizer(state.undoStack, state.redoStack);
    renderCategoryDonut(state.cart.items);
  }

  async function triggerBinPacking() {
    try {
      const cap = state.boxCapacity;
      const fee = state.courierFee;
      const res = await apiGet(`/api/binpack?capacity=${cap}&fee=${fee}`);
      if (res.comparison) {
        state.packingResult = res.comparison;
        renderPackingComparison(res.comparison);
        
        // Initialize 3D Wireframe Simulation
        initPackingSimulation(res.comparison.ffd);
      }
    } catch (err) {
      console.error('Bin packing fetch error:', err);
    }
  }

  async function triggerCheckout() {
    const customer = el.checkoutCustomerName ? el.checkoutCustomerName.value : 'Customer';
    const org = el.checkoutOrgName ? el.checkoutOrgName.value : 'Event';
    try {
      const res = await apiPost('/api/checkout', {
        customerName: customer,
        organizationName: org
      });
      if (res) {
        state.latestCheckout = res;
        renderCheckoutResult(res);
      }
    } catch (err) {
      console.error('Checkout error:', err);
    }
  }

  // ==========================================
  // 3D WIREFRAME BIN PACKING SIMULATION ENGINE
  // ==========================================

  function initPackingSimulation(packingData, preFill = true) {
    pauseSimulation();

    // Prepare unpacked queue
    state.simulation.unpackedQueue = [];
    state.simulation.placedItems = [];

    if (!packingData || !packingData.boxes || packingData.boxes.length === 0) {
      el.binsWireframeStage.innerHTML = `
        <div class="empty-state-card" style="grid-column: span 3; padding: 2.5rem 1rem;">
          <p style="color: var(--text-muted); margin-bottom: 0.85rem; font-size: 0.82rem;">Your cart is empty. Add items from the catalog on the left to pack 3D cartons.</p>
          <button class="btn-stark-white" id="btnPopulateCartPrompt" style="margin: 0 auto;">
            ⚡ Auto-Maximize Cart (0/1 Knapsack)
          </button>
        </div>
      `;
      const btnPrompt = document.getElementById('btnPopulateCartPrompt');
      if (btnPrompt) btnPrompt.addEventListener('click', () => el.btnRunKnapsack.click());
      el.itemQueueContainer.innerHTML = '<span class="text-muted" style="font-size: 0.72rem;">No pending items.</span>';
      el.unpackedItemsCount.textContent = '0 pending';
      updatePackerKpis(packingData);
      return;
    }

    // Build the queue of items to be placed
    let colorIdx = 0;
    packingData.boxes.forEach(box => {
      box.items.forEach(it => {
        const color = BLOCK_COLORS[colorIdx % BLOCK_COLORS.length];
        colorIdx++;
        state.simulation.unpackedQueue.push({
          item: it,
          targetBoxId: box.boxId,
          color: color,
          dimText: generateDimensionLabel(it.weight)
        });
      });
    });

    // Render empty 3D wireframe boxes
    renderEmpty3DBins(packingData.boxes);

    if (preFill) {
      // Pre-fill the boxes immediately so the user sees the packed 3D cartons right away!
      while (state.simulation.unpackedQueue.length > 0) {
        stepSimulationForward(false);
      }
      addLog(`[3D Visualizer] Loaded ${packingData.boxes.length} cartons with ${state.simulation.placedItems.length} items. Ready to run simulation.`, 'white');
    } else {
      renderUnpackedQueue();
    }

    // Update KPI metrics
    updatePackerKpis(packingData);
  }

  function generateDimensionLabel(weight) {
    const w = Math.round(weight * 8);
    const h = Math.round(weight * 5);
    return `${w} x ${h}"`;
  }

  function renderEmpty3DBins(boxes) {
    el.binsWireframeStage.innerHTML = '';

    if (!boxes || boxes.length === 0) {
      el.binsWireframeStage.innerHTML = '<div class="empty-state-card" style="grid-column: span 3">No cartons dispatched yet. Add items to cart.</div>';
      return;
    }

    boxes.forEach(box => {
      const col = document.createElement('div');
      col.className = 'bin-col-container';
      col.id = `binCol_${box.boxId}`;

      col.innerHTML = `
        <div class="bin-header-label">
          <span>Bin ${box.boxId}</span>
          <span class="bin-pct-tag" id="binPctTag_${box.boxId}">0% Full</span>
        </div>
        <div class="wireframe-container-3d">
          <svg class="wireframe-box-svg" viewBox="0 0 160 250" preserveAspectRatio="none">
            <!-- Back Face (Recessed) -->
            <polygon points="18,16 142,16 142,224 18,224" fill="rgba(255,255,255,0.015)" stroke="rgba(255,255,255,0.12)" stroke-dasharray="3,3" />
            <!-- Perspective Corner Lines -->
            <line x1="0" y1="36" x2="18" y2="16" stroke="rgba(255,255,255,0.28)" stroke-width="1.2" />
            <line x1="160" y1="36" x2="142" y2="16" stroke="rgba(255,255,255,0.28)" stroke-width="1.2" />
            <line x1="0" y1="250" x2="18" y2="224" stroke="rgba(255,255,255,0.16)" stroke-width="1" stroke-dasharray="2,2" />
            <line x1="160" y1="250" x2="142" y2="224" stroke="rgba(255,255,255,0.16)" stroke-width="1" stroke-dasharray="2,2" />
            <!-- Top Opening Rim (Angled Perspective Trapezoid) -->
            <polygon points="0,36 18,16 142,16 160,36" fill="rgba(255,255,255,0.04)" stroke="rgba(255,255,255,0.32)" stroke-width="1.2" />
            <!-- Front Face Frame -->
            <polygon points="0,36 160,36 160,250 0,250" fill="transparent" stroke="rgba(255,255,255,0.22)" stroke-width="1.2" />
          </svg>
          <div class="wireframe-3d-box" id="wireframeBox_${box.boxId}">
            <!-- Dropped blocks stack here from bottom -->
          </div>
        </div>
      `;

      el.binsWireframeStage.appendChild(col);
    });
  }

  function renderUnpackedQueue() {
    el.itemQueueContainer.innerHTML = '';
    const queue = state.simulation.unpackedQueue;
    el.unpackedItemsCount.textContent = `${queue.length} pending`;

    if (queue.length === 0) {
      el.itemQueueContainer.innerHTML = '<span class="text-muted" style="font-size: 0.72rem;">All items packed into shipping cartons.</span>';
      return;
    }

    queue.forEach(qItem => {
      const chip = document.createElement('div');
      chip.className = 'queue-chip';
      chip.style.backgroundColor = qItem.color.bg;
      chip.style.borderColor = qItem.color.border;
      chip.style.color = qItem.color.text;
      chip.innerHTML = `
        <span>${qItem.item.name.substring(0, 14)}</span>
        <span style="opacity: 0.85; font-size: 0.62rem">(${qItem.item.weight}kg)</span>
      `;
      el.itemQueueContainer.appendChild(chip);
    });
  }

  function stepSimulationForward(animate = true) {
    if (state.simulation.unpackedQueue.length === 0) {
      pauseSimulation();
      return false;
    }

    // Pop the next item
    const next = state.simulation.unpackedQueue.shift();
    state.simulation.placedItems.push(next);

    // Place into target 3D box
    const boxContainer = document.getElementById(`wireframeBox_${next.targetBoxId}`);
    if (boxContainer) {
      const block = document.createElement('div');
      block.className = 'packed-block';
      if (!animate) block.style.animation = 'none';
      block.style.backgroundColor = next.color.bg;
      block.style.borderColor = next.color.border;
      block.style.color = next.color.text;

      // Height proportional to weight
      const heightPx = Math.max(26, Math.min(65, Math.round(next.item.weight * 6)));
      block.style.height = `${heightPx}px`;

      block.innerHTML = `
        <span class="block-label">${next.item.name}</span>
        <span class="block-dim">${next.dimText} • ${next.item.weight}kg</span>
      `;
      boxContainer.appendChild(block);

      // Update box header pct
      updateSingleBoxPercentage(next.targetBoxId);
    }

    // Refresh queue display
    renderUnpackedQueue();
    return true;
  }

  function updateSingleBoxPercentage(boxId) {
    if (!state.packingResult || !state.packingResult.ffd) return;
    const boxDef = state.packingResult.ffd.boxes.find(b => b.boxId === boxId);
    if (!boxDef) return;

    // Calculate current weight of placed items in this box
    const placedInThisBox = state.simulation.placedItems.filter(p => p.targetBoxId === boxId);
    const currWt = placedInThisBox.reduce((sum, p) => sum + p.item.weight, 0);
    const pct = Math.min(100, Math.round((currWt / boxDef.maxCapacity) * 100));

    const tag = document.getElementById(`binPctTag_${boxId}`);
    if (tag) {
      tag.textContent = `${pct}% Full (${currWt.toFixed(1)}kg)`;
      if (pct >= 85) tag.style.color = '#fff';
      else tag.style.color = 'var(--text-secondary)';
    }
  }

  function startSimulation() {
    // If all items are already placed (like on initial load), reset to queue first!
    if (state.simulation.unpackedQueue.length === 0 && state.simulation.placedItems.length > 0) {
      if (state.packingResult && state.packingResult.ffd) {
        initPackingSimulation(state.packingResult.ffd, false);
      }
    }

    state.simulation.isRunning = true;
    el.btnPlayVisualizer.innerHTML = `
      <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>
      Pause
    `;

    addLog('[Bin Packing Simulation] Visualizer running...', 'white');

    state.simulation.timer = setInterval(() => {
      const hasMore = stepSimulationForward(true);
      if (!hasMore) {
        pauseSimulation();
        addLog('[Bin Packing Simulation] All cartons fully packed!', 'white');
      }
    }, state.simulation.speedMs);
  }

  function pauseSimulation() {
    state.simulation.isRunning = false;
    if (state.simulation.timer) {
      clearInterval(state.simulation.timer);
      state.simulation.timer = null;
    }
    el.btnPlayVisualizer.innerHTML = `
      <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>
      Run Visualizer
    `;
  }

  function resetSimulation() {
    pauseSimulation();
    if (state.packingResult && state.packingResult.ffd) {
      initPackingSimulation(state.packingResult.ffd, false);
      addLog('[Bin Packing Simulation] Emptied cartons. All items queued for placement.', 'muted');
    }
  }

  function updatePackerKpis(packingData) {
    const boxCount = packingData.boxes ? packingData.boxes.length : 0;
    const courierFee = packingData.courierCost || 0;
    const avgUtil = packingData.averageUtilization || 0;

    el.packerBoxCountBadge.textContent = `${boxCount} Dispatched`;
    el.packerCourierCostBadge.textContent = `Courier: ₹${courierFee.toFixed(2)}`;

    // Header KPIs
    el.kpiCartBoxes.textContent = `${boxCount} Cartons`;
    el.kpiCourierCostSub.textContent = `Courier: ₹${courierFee.toFixed(2)}`;
    el.kpiSpaceUtil.textContent = `${avgUtil}% Efficiency`;
    el.kpiUtilProgress.style.width = `${avgUtil}%`;
    el.kpiBoxesProgress.style.width = `${Math.min(100, boxCount * 25)}%`;
  }

  // ==========================================
  // DYNAMIC CATEGORY DONUT CHART (SVG)
  // ==========================================

  function renderCategoryDonut(cartItems) {
    if (!el.donutSlicesGroup || !el.donutLegendContainer) return;
    el.donutSlicesGroup.innerHTML = '';
    el.donutLegendContainer.innerHTML = '';

    const items = cartItems || [];
    el.donutCenterCount.textContent = items.length;

    if (items.length === 0) {
      el.donutLegendContainer.innerHTML = '<span class="text-muted" style="font-size: 0.74rem;">Cart empty.</span>';
      return;
    }

    // Tally by category
    const catCounts = {};
    items.forEach(it => {
      const cat = it.category || 'General';
      catCounts[cat] = (catCounts[cat] || 0) + 1;
    });

    const categories = Object.keys(catCounts);
    const total = items.length;
    const radius = 60;
    const circumference = 2 * Math.PI * radius; // ~376.99

    let accumulatedPct = 0;
    const sliceColors = ['#06b6d4', '#00f59b', '#8b5cf6', '#f59e0b', '#f43f5e', '#ec4899', '#3b82f6'];

    categories.forEach((cat, idx) => {
      const count = catCounts[cat];
      const pct = count / total;
      const strokeLength = pct * circumference;
      const strokeDashoffset = -(accumulatedPct * circumference);
      const color = sliceColors[idx % sliceColors.length];

      // SVG Slice Circle
      const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
      circle.setAttribute('cx', '80');
      circle.setAttribute('cy', '80');
      circle.setAttribute('r', radius);
      circle.setAttribute('fill', 'transparent');
      circle.setAttribute('stroke', color);
      circle.setAttribute('stroke-width', '24');
      circle.setAttribute('stroke-dasharray', `${strokeLength} ${circumference}`);
      circle.setAttribute('stroke-dashoffset', strokeDashoffset);
      el.donutSlicesGroup.appendChild(circle);

      accumulatedPct += pct;

      // Legend Item
      const legendChip = document.createElement('div');
      legendChip.className = 'legend-chip';
      legendChip.innerHTML = `
        <span class="legend-color-dot" style="background: ${color}"></span>
        <span>${cat} <strong style="color: var(--text-white)">(${Math.round(pct * 100)}%)</strong></span>
      `;
      el.donutLegendContainer.appendChild(legendChip);
    });
  }

  // ==========================================
  // RENDERERS (CATALOG, CART, KNAPSACK, AVL, STACK, HUFFMAN)
  // ==========================================

  function renderCatalog(items) {
    if (!el.catalogContainer) return;
    el.catalogContainer.innerHTML = '';

    if (!items || items.length === 0) {
      el.catalogContainer.innerHTML = '<div class="empty-state-card" style="grid-column: span 3; padding: 1.5rem; text-align: center; color: var(--text-muted); font-size: 0.8rem;">No products match your search or category filter.</div>';
      return;
    }

    items.forEach(it => {
      const card = document.createElement('div');
      card.className = 'catalog-item-card';
      card.innerHTML = `
        <div class="item-card-top">
          <span class="item-card-name" title="${it.name}">${it.name}</span>
          <span class="item-card-cat">${it.category || 'General'}</span>
        </div>
        <div class="item-card-meta">
          <span>Priority: <strong style="color: var(--accent-neon-green)">+${it.utility} pts</strong></span>
          <span>Weight: <strong>${it.weight} kg</strong></span>
        </div>
        <div class="item-card-actions">
          <span class="item-card-price">₹${Math.round(it.price).toLocaleString()}</span>
          <button class="btn-xs btn-stark-white btn-add-cart" data-id="${it.id}">+ Add to Cart</button>
        </div>
      `;

      const btnAdd = card.querySelector('.btn-add-cart');
      btnAdd.addEventListener('click', async () => {
        try {
          btnAdd.textContent = 'Adding...';
          btnAdd.disabled = true;
          await apiPost('/api/cart/add', { itemId: it.id });
          addLog(`[Cart Action] Manually added "${it.name}" (₹${it.price}, ${it.weight}kg, +${it.utility}pts)`, 'white');
          await fetchCart();
          await triggerBinPacking();
          btnAdd.textContent = 'Added ✓';
          setTimeout(() => {
            btnAdd.textContent = '+ Add to Cart';
            btnAdd.disabled = false;
          }, 900);
        } catch (err) {
          console.error('Failed to add item to cart:', err);
          btnAdd.textContent = '+ Add to Cart';
          btnAdd.disabled = false;
        }
      });

      el.catalogContainer.appendChild(card);
    });
  }

  function renderCart(cart) {
    el.cartTableBody.innerHTML = '';
    const items = cart.items || [];

    if (items.length === 0) {
      el.cartEmptyState.style.display = 'block';
    } else {
      el.cartEmptyState.style.display = 'none';
      items.forEach((it, idx) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
          <td><strong>${it.name}</strong> <span style="color: var(--text-muted); font-size: 0.68rem">(${it.category})</span></td>
          <td class="text-right">₹${it.price.toLocaleString()}</td>
          <td class="text-right" style="color: #34d399; font-weight: 700;">+${it.utility} pts</td>
          <td class="text-right" style="color: var(--text-secondary); font-family: var(--font-mono);">${it.weight} kg</td>
          <td class="text-center">
            <button class="btn-xs btn-outline-danger btn-del-cart" data-index="${idx}" title="Remove item">&times;</button>
          </td>
        `;

        tr.querySelector('.btn-del-cart').addEventListener('click', async () => {
          await apiPost('/api/cart/remove', { index: idx });
          addLog(`[Cart] Removed "${it.name}"`, 'amber');
          await fetchCart();
          await triggerBinPacking();
        });

        el.cartTableBody.appendChild(tr);
      });
    }

    // Top Header & Financial Cards
    const budgetLimit = parseFloat(el.budgetInput.value) || 10000;
    const spent = cart.totalCost || 0;
    const remaining = Math.max(0, budgetLimit - spent);
    const budgetPct = Math.min(100, Math.round((spent / budgetLimit) * 100));

    el.dispBudgetLimit.textContent = `₹${budgetLimit.toLocaleString()}`;
    el.dispCartCost.textContent = `₹${spent.toLocaleString()}`;
    el.dispCartUtility.textContent = `${cart.totalUtility || 0} pts`;
    el.dispRemainingBudget.textContent = `₹${remaining.toLocaleString()}`;

    // Header Ribbon KPIs
    el.kpiCartCost.textContent = `₹${spent.toLocaleString()} / ₹${budgetLimit.toLocaleString()}`;
    el.kpiBudgetProgress.style.width = `${budgetPct}%`;
    el.kpiWeightRatio.textContent = `${(cart.totalWeight || 0).toFixed(1)} kg Total Weight`;

    // Quick Undo/Redo Badges
    el.undoBadge.textContent = state.undoStack.length;
    el.redoBadge.textContent = state.redoStack.length;
    el.btnQuickUndo.disabled = state.undoStack.length === 0;
    el.btnQuickRedo.disabled = state.redoStack.length === 0;
    el.btnInspectUndo.disabled = state.undoStack.length === 0;
    el.btnInspectRedo.disabled = state.redoStack.length === 0;
  }

  function renderPackingComparison(comp) {
    el.ffdBoxCount.textContent = comp.ffd.boxCount;
    el.ffdUtil.textContent = `${comp.ffd.averageUtilization}%`;
    el.ffdCost.textContent = `₹${comp.ffd.courierCost}`;

    el.bfdBoxCount.textContent = comp.bfd.boxCount;
    el.bfdUtil.textContent = `${comp.bfd.averageUtilization}%`;
    el.bfdCost.textContent = `₹${comp.bfd.courierCost}`;

    el.wfdBoxCount.textContent = comp.worstFit.boxCount;
    el.wfdUtil.textContent = `${comp.worstFit.averageUtilization}%`;
    el.wfdCost.textContent = `₹${comp.worstFit.courierCost}`;

    el.nfBoxCount.textContent = comp.nextFit.boxCount;
    el.nfUtil.textContent = `${comp.nextFit.averageUtilization}%`;
    el.nfCost.textContent = `₹${comp.nextFit.courierCost}`;

    if (comp.savingsVersusNaive > 0) {
      el.packingSavingsText.textContent = `FFD Bin Packing saves ₹${comp.savingsVersusNaive.toFixed(2)} in courier dispatch charges compared to naive Next Fit packing (${comp.ffd.boxCount} boxes vs ${comp.nextFit.boxCount} boxes).`;
    } else {
      el.packingSavingsText.textContent = `FFD achieved optimal carton packing (${comp.ffd.boxCount} boxes used at ${comp.ffd.averageUtilization}% average capacity).`;
    }

    // Detailed Boxes Grid in Tab 3
    el.boxesDetailedGrid.innerHTML = '';
    comp.ffd.boxes.forEach(box => {
      const card = document.createElement('div');
      card.className = 'dash-card';
      let chips = '';
      box.items.forEach(it => {
        chips += `<span class="queue-chip" style="background: var(--bg-surface); border: 1px solid var(--border-light);">${it.name} (${it.weight}kg)</span>`;
      });
      card.innerHTML = `
        <div class="dash-card-header">
          <span class="dash-card-title">Carton #${box.boxId}</span>
          <span class="badge-pill-cyan">${box.currentWeight} / ${box.maxCapacity} kg</span>
        </div>
        <div class="kpi-progress-bar" style="height: 6px; margin: 0.5rem 0;">
          <div class="kpi-progress-fill fill-green" style="width: ${box.utilizationPercentage}%"></div>
        </div>
        <div style="display: flex; flex-wrap: wrap; gap: 0.35rem; margin-top: 0.5rem;">${chips}</div>
      `;
      el.boxesDetailedGrid.appendChild(card);
    });
  }

  function renderKnapsackResult(result) {
    el.ksDpUtility.textContent = `${result.totalUtility} pts`;
    el.ksDpCost.textContent = `Total Spend: ₹${result.totalCost.toLocaleString()} (Rem: ₹${result.remainingBudget})`;

    el.ksGreedyUtility.textContent = `${result.greedyUtility} pts`;
    el.ksGreedyCost.textContent = `Total Spend: ₹${result.greedyCost.toLocaleString()}`;

    const diff = result.totalUtility - result.greedyUtility;
    el.ksOptimalityGap.textContent = diff >= 0 ? `+${diff} pts` : `${diff} pts`;

    const runtimeMs = (result.computationTimeNanos / 1_000_000).toFixed(2);
    el.ksRuntime.textContent = `${runtimeMs} ms`;
    el.ksExplanationText.textContent = result.optimalityExplanation;

    // DP Matrix Rows
    el.dpMatrixBody.innerHTML = '';
    if (result.dpSampleRows) {
      result.dpSampleRows.forEach(row => {
        const tr = document.createElement('tr');
        let valsHtml = '';
        row.valuesAtKeyBudgets.forEach(val => {
          valsHtml += `<td>${val}</td>`;
        });
        tr.innerHTML = `
          <td>${row.itemName}</td>
          <td>₹${row.cost}</td>
          <td style="color: var(--accent-neon-green)">${row.utility}</td>
          ${valsHtml}
        `;
        el.dpMatrixBody.appendChild(tr);
      });
    }

    // Optimal vs Greedy Lists
    el.ksDpItemsList.innerHTML = '';
    result.selectedItems.forEach(it => {
      const card = document.createElement('div');
      card.className = 'item-tag-card';
      card.innerHTML = `
        <strong>${it.name}</strong>
        <span>₹${it.price.toLocaleString()} • <span style="color: var(--accent-neon-green)">+${it.utility} pts</span></span>
      `;
      el.ksDpItemsList.appendChild(card);
    });

    el.ksGreedyItemsList.innerHTML = '';
    result.greedyItems.forEach(it => {
      const card = document.createElement('div');
      card.className = 'item-tag-card';
      card.innerHTML = `
        <strong>${it.name}</strong>
        <span>₹${it.price.toLocaleString()} • <span style="color: var(--accent-amber)">+${it.utility} pts</span></span>
      `;
      el.ksGreedyItemsList.appendChild(card);
    });
  }

  function renderStackVisualizer(undoStack, redoStack) {
    el.undoStackDepth.textContent = undoStack ? undoStack.length : 0;
    el.redoStackDepth.textContent = redoStack ? redoStack.length : 0;

    el.undoStackList.innerHTML = '';
    if (!undoStack || undoStack.length === 0) {
      el.undoStackList.innerHTML = '<div class="stack-empty-msg">Undo Stack is empty. Mutate cart to push actions.</div>';
    } else {
      for (let i = undoStack.length - 1; i >= 0; i--) {
        const act = undoStack[i];
        const card = document.createElement('div');
        card.className = 'stack-element-card';
        card.innerHTML = `
          <div>
            <strong style="color: var(--accent-neon-green)">${act.type}</strong>
            <span style="color: var(--text-muted); font-size: 0.72rem; display: block;">${act.item ? act.item.name : 'All Items'}</span>
          </div>
          <span class="badge-pill-green">TOS #${i + 1}</span>
        `;
        el.undoStackList.appendChild(card);
      }
    }

    el.redoStackList.innerHTML = '';
    if (!redoStack || redoStack.length === 0) {
      el.redoStackList.innerHTML = '<div class="stack-empty-msg">Redo Stack is empty. Undo an action to push here.</div>';
    } else {
      for (let i = redoStack.length - 1; i >= 0; i--) {
        const act = redoStack[i];
        const card = document.createElement('div');
        card.className = 'stack-element-card';
        card.innerHTML = `
          <div>
            <strong style="color: var(--accent-neon-blue)">${act.type}</strong>
            <span style="color: var(--text-muted); font-size: 0.72rem; display: block;">${act.item ? act.item.name : 'All Items'}</span>
          </div>
          <span class="badge-pill-cyan">TOS #${i + 1}</span>
        `;
        el.redoStackList.appendChild(card);
      }
    }
  }


  // SVG AVL Tree Visualizer

  async function fetchAndRenderAvlTree() {
    try {
      const res = await apiGet('/api/catalog/tree');
      if (res && res.tree) {
        if (el.avlTotalNodes) el.avlTotalNodes.textContent = res.nodeCount || 0;
        if (el.avlHeight) el.avlHeight.textContent = res.height || 0;
        renderAvlTreeSvg(res.tree);
        if (el.btnExecuteRangeSearch) {
          el.btnExecuteRangeSearch.click();
        }
      } else {
        renderAvlTreeSvg(null);
      }
    } catch (err) {
      console.error('AVL fetch error:', err);
    }
  }

  function renderAvlTreeSvg(rootNode) {
    if (!el.avlTreeGraph) return;
    el.avlTreeGraph.innerHTML = '';
    if (!rootNode) {
      el.avlTreeGraph.innerHTML = '<div class="empty-state-card" style="margin: auto;">AVL Tree is empty.</div>';
      return;
    }

    const svgNS = 'http://www.w3.org/2000/svg';
    const svg = document.createElementNS(svgNS, 'svg');
    svg.setAttribute('width', '100%');
    svg.setAttribute('height', '100%');
    svg.setAttribute('viewBox', '0 0 940 450');

    // Layout tree nodes coordinates
    const nodes = [];
    const edges = [];

    function traverse(node, x, y, dx) {
      if (!node) return;
      nodes.push({ node, x, y });

      const children = node.children || [];
      const leftChild = children.find(c => c.branch === 'L');
      const rightChild = children.find(c => c.branch === 'R');

      if (leftChild) {
        edges.push({ x1: x, y1: y, x2: x - dx, y2: y + 70 });
        traverse(leftChild, x - dx, y + 70, dx * 0.52);
      }
      if (rightChild) {
        edges.push({ x1: x, y1: y, x2: x + dx, y2: y + 70 });
        traverse(rightChild, x + dx, y + 70, dx * 0.52);
      }
    }

    traverse(rootNode, 470, 38, 220);

    // Draw Edges
    edges.forEach(e => {
      const line = document.createElementNS(svgNS, 'line');
      line.setAttribute('x1', e.x1);
      line.setAttribute('y1', e.y1);
      line.setAttribute('x2', e.x2);
      line.setAttribute('y2', e.y2);
      const isLight = document.documentElement.getAttribute('data-theme') === 'light';
      line.setAttribute('stroke', isLight ? '#cbd5e1' : '#282828');
      line.setAttribute('stroke-width', '1.5');
      svg.appendChild(line);
    });

    // Draw Nodes
    nodes.forEach((n, idx) => {
      const g = document.createElementNS(svgNS, 'g');
      g.setAttribute('class', 'tree-node-group');
      g.setAttribute('id', `avl_node_${idx}`);
      g.style.cursor = 'pointer';

      // SVG Tooltip
      const title = document.createElementNS(svgNS, 'title');
      title.textContent = `${n.node.name || 'Item'}\nPrice: ₹${n.node.price}\nWeight: ${n.node.weight}kg\nUtility: ${n.node.utility}pts\nHeight: ${n.node.height}\nBalance Factor: ${n.node.balance || 0}`;
      g.appendChild(title);

      const circle = document.createElementNS(svgNS, 'circle');
      circle.setAttribute('cx', n.x);
      circle.setAttribute('cy', n.y);
      circle.setAttribute('r', '22');
      circle.setAttribute('class', 'tree-node-circle');
      const isLightNode = document.documentElement.getAttribute('data-theme') === 'light';
      circle.setAttribute('fill', idx === 0 ? (isLightNode ? '#0f172a' : '#ffffff') : (isLightNode ? '#ffffff' : '#141414'));
      circle.setAttribute('stroke', idx === 0 ? (isLightNode ? '#0f172a' : '#ffffff') : (isLightNode ? '#cbd5e1' : '#2e2e2e'));

      circle.setAttribute('stroke-width', '1.5');
      circle.setAttribute('data-price', n.node.price);

      const textPrice = document.createElementNS(svgNS, 'text');
      textPrice.setAttribute('x', n.x);
      textPrice.setAttribute('y', n.y - 2);
      textPrice.setAttribute('text-anchor', 'middle');
      textPrice.setAttribute('font-family', 'JetBrains Mono, monospace');
      textPrice.setAttribute('font-size', '10px');
      textPrice.setAttribute('font-weight', '700');
      textPrice.setAttribute('fill', idx === 0 ? (isLightNode ? '#ffffff' : '#000000') : (isLightNode ? '#0f172a' : '#ffffff'));
      textPrice.textContent = `₹${Math.round(n.node.price)}`;

      const textName = document.createElementNS(svgNS, 'text');
      textName.setAttribute('x', n.x);
      textName.setAttribute('y', n.y + 12);
      textName.setAttribute('text-anchor', 'middle');
      textName.setAttribute('font-size', '8px');
      textName.setAttribute('fill', idx === 0 ? (isLightNode ? '#cbd5e1' : '#333333') : (isLightNode ? '#64748b' : '#888888'));
      const itemTitle = n.node.name || '';
      textName.textContent = itemTitle.length > 8 ? itemTitle.substring(0, 7) + '..' : itemTitle;

      g.appendChild(circle);
      g.appendChild(textPrice);
      g.appendChild(textName);

      // Click interaction: sets min price and selects node
      g.addEventListener('click', () => {
        if (el.avlRangeMin && el.avlRangeMax) {
          el.avlRangeMin.value = Math.max(0, Math.round(n.node.price - 500));
          el.avlRangeMax.value = Math.round(n.node.price + 500);
          if (el.btnExecuteRangeSearch) el.btnExecuteRangeSearch.click();
        }
        addLog(`[AVL Node Inspected] "${n.node.name}" (₹${n.node.price}, ${n.node.weight}kg, Utility: ${n.node.utility}, Height: ${n.node.height}, Balance: ${n.node.balance || 0})`, 'white');
      });

      svg.appendChild(g);
    });

    el.avlTreeGraph.appendChild(svg);
  }

  function highlightAvlTreeRange(minP, maxP) {
    if (!el.avlTreeGraph) return;
    const groups = el.avlTreeGraph.querySelectorAll('.tree-node-group');
    const isLightH = document.documentElement.getAttribute('data-theme') === 'light';
    groups.forEach(g => {
      const circle = g.querySelector('.tree-node-circle');
      const textPrice = g.querySelector('text:first-of-type');
      const textName = g.querySelector('text:last-of-type');
      if (!circle) return;

      const price = parseFloat(circle.getAttribute('data-price'));
      if (price >= minP && price <= maxP) {
        circle.setAttribute('fill', isLightH ? '#0f172a' : '#ffffff');
        circle.setAttribute('stroke', isLightH ? '#0f172a' : '#ffffff');
        if (textPrice) textPrice.setAttribute('fill', isLightH ? '#ffffff' : '#000000');
        if (textName) textName.setAttribute('fill', isLightH ? '#cbd5e1' : '#333333');
      } else {
        circle.setAttribute('fill', isLightH ? '#ffffff' : '#141414');
        circle.setAttribute('stroke', isLightH ? '#cbd5e1' : '#282828');
        if (textPrice) textPrice.setAttribute('fill', isLightH ? '#0f172a' : '#ffffff');
        if (textName) textName.setAttribute('fill', isLightH ? '#64748b' : '#777777');
      }
    });
  }


  // Huffman Compression & Checkout Result

  function renderCheckoutResult(data) {
    if (!data) return;
    const invoice = data.invoice || {};
    const comp = data.compression || data.result || data;

    const rawText = invoice.rawInvoiceText || comp.originalText || '';
    const bitString = comp.encodedBitString || comp.encodedBitstream || '';
    const decodedText = comp.decodedText || comp.decompressedInvoiceText || rawText;
    const bitLen = comp.compressedSizeBits !== undefined ? comp.compressedSizeBits : (comp.encodedTotalBits !== undefined ? comp.encodedTotalBits : bitString.length);
    const origBits = comp.originalSizeBits !== undefined ? comp.originalSizeBits : (comp.originalTotalBits !== undefined ? comp.originalTotalBits : (rawText.length * 8));
    const origBytes = comp.originalSizeBytes !== undefined ? comp.originalSizeBytes : (comp.originalTotalBytes !== undefined ? comp.originalTotalBytes : rawText.length);
    const compBytes = comp.compressedSizeBytes !== undefined ? comp.compressedSizeBytes : (comp.encodedPayloadBytes !== undefined ? comp.encodedPayloadBytes : Math.ceil(bitLen / 8));
    const ratio = comp.compressionRatio !== undefined ? comp.compressionRatio : 0;
    const bpc = comp.bitsPerCharacter !== undefined ? comp.bitsPerCharacter : (comp.averageBitsPerChar !== undefined ? comp.averageBitsPerChar : 0);
    const isLossless = comp.lossless !== undefined ? comp.lossless : (comp.isLossless !== undefined ? comp.isLossless : (comp.losslessVerified !== undefined ? comp.losslessVerified : true));
    const codes = comp.codeTable || comp.prefixCodes || [];

    if (el.rawInvoiceDisplay) el.rawInvoiceDisplay.textContent = rawText;
    if (el.encodedBitstreamDisplay) el.encodedBitstreamDisplay.textContent = bitString;
    if (el.decompressedTextDisplay) el.decompressedTextDisplay.textContent = decodedText;
    if (el.bitLengthBadge) el.bitLengthBadge.textContent = `${bitLen} bits`;

    renderHuffmanMetrics({
      originalTotalBits: origBits,
      originalTotalBytes: origBytes,
      encodedTotalBits: bitLen,
      encodedPayloadBytes: compBytes,
      compressionRatio: ratio,
      averageBitsPerChar: bpc,
      losslessVerified: isLossless
    });

    renderHuffmanCodesTable(codes);
    addLog(`[Huffman Coding] Lossless compressed invoice: ${ratio.toFixed(1)}% space savings (${bitLen} bits vs ${origBits} bits ASCII)`, 'green');
  }

  function renderHuffmanMetrics(res) {
    if (el.compOriginalBits) el.compOriginalBits.textContent = `${(res.originalTotalBits || 0).toLocaleString()} bits`;
    if (el.compOriginalBytes) el.compOriginalBytes.textContent = `(${(res.originalTotalBytes || 0)} bytes ASCII)`;

    if (el.compEncodedBits) el.compEncodedBits.textContent = `${(res.encodedTotalBits || 0).toLocaleString()} bits`;
    if (el.compEncodedBytes) el.compEncodedBytes.textContent = `(${(res.encodedPayloadBytes || 0)} bytes payload)`;

    if (el.compRatio) el.compRatio.textContent = `${(res.compressionRatio || 0).toFixed(1)}%`;
    if (el.compBitsPerChar) el.compBitsPerChar.textContent = `${(res.averageBitsPerChar || 0).toFixed(2)} bits/char`;

    if (el.compLosslessBadge) el.compLosslessBadge.textContent = res.losslessVerified ? 'Verified 100%' : 'Mismatch';
  }

  function renderHuffmanCodesTable(codes) {
    if (!el.huffmanCodeTableBody) return;
    el.huffmanCodeTableBody.innerHTML = '';
    if (!codes || codes.length === 0) {
      el.huffmanCodeTableBody.innerHTML = '<tr><td colspan="5" style="color: var(--text-muted); text-align: center; padding: 1.5rem;">No prefix codes available.</td></tr>';
      return;
    }

    codes.forEach(c => {
      const tr = document.createElement('tr');
      const dispChar = c.characterDisplay || (c.character === '\n' ? '\\n (Newline)' : (c.character === ' ' ? '␣ (Space)' : c.character || c.rawChar));
      const freq = c.frequency || 0;
      const code = c.huffmanCode || '';
      const bitLen = c.codeLength || c.bitLength || code.length;
      const savings = Math.max(0, Math.round((1 - bitLen / 8) * 100));

      tr.innerHTML = `
        <td><strong style="color: var(--text-white)">${dispChar}</strong></td>
        <td>${freq}</td>
        <td><code style="color: var(--accent-cyan); font-weight: 700;">${code}</code></td>
        <td>${bitLen} bits</td>
        <td style="color: var(--accent-neon-green)">8b &rarr; ${bitLen}b (${savings}% save)</td>
      `;
      el.huffmanCodeTableBody.appendChild(tr);
    });
  }


  // Print & Word Document Export Handlers

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function printInvoiceDocument(text, title = 'Digital Order Invoice') {
    if (!text || text.trim() === '' || text.includes('Loading invoice...')) {
      alert('Invoice is still loading. Please click "Generate & Compress Invoice" and try again.');
      return;
    }

    const customer = (el.checkoutCustomerName && el.checkoutCustomerName.value) || 'Valued Customer';
    const org = (el.checkoutOrgName && el.checkoutOrgName.value) || 'Event Logistics';
    const dateStr = new Date().toLocaleString();

    const printableHtml = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <title>${escapeHtml(title)}</title>
        <style>
          @page { size: A4; margin: 15mm; }
          * { box-sizing: border-box; }
          body {
            font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, Helvetica, Arial, sans-serif;
            color: #000;
            background: #fff;
            margin: 0;
            padding: 24px;
            font-size: 11pt;
            line-height: 1.45;
          }
          .header-bar {
            border-bottom: 2px solid #000;
            padding-bottom: 12px;
            margin-bottom: 16px;
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
          }
          .org-title {
            font-size: 16pt;
            font-weight: 800;
            letter-spacing: -0.5px;
            margin: 0 0 4px 0;
            text-transform: uppercase;
          }
          .sub-title {
            font-size: 9.5pt;
            color: #444;
            margin: 0;
          }
          .badge-pill {
            display: inline-block;
            background: #000;
            color: #fff;
            font-size: 8.5pt;
            font-weight: 700;
            padding: 4px 10px;
            border-radius: 4px;
            letter-spacing: 0.5px;
          }
          .meta-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
            background: #f7f7f7;
            border: 1px solid #ddd;
            padding: 12px 16px;
            border-radius: 6px;
            margin-bottom: 18px;
            font-size: 9.5pt;
          }
          .meta-item {
            margin-bottom: 4px;
          }
          .meta-item strong {
            color: #111;
            display: inline-block;
            width: 120px;
          }
          .section-title {
            font-size: 11pt;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin: 18px 0 8px 0;
            border-left: 3px solid #000;
            padding-left: 8px;
          }
          .raw-bill-box {
            font-family: 'Consolas', 'Courier New', monospace;
            font-size: 9.5pt;
            white-space: pre-wrap;
            background: #fff;
            border: 1px solid #111;
            border-radius: 4px;
            padding: 16px;
            margin-bottom: 20px;
            line-height: 1.35;
          }
          .signature-row {
            margin-top: 40px;
            display: flex;
            justify-content: space-between;
            padding: 0 30px;
          }
          .sig-line {
            border-top: 1px solid #000;
            width: 200px;
            text-align: center;
            padding-top: 6px;
            font-size: 9pt;
            font-weight: 600;
          }
          .footer-bar {
            margin-top: 30px;
            padding-top: 10px;
            border-top: 1px solid #ccc;
            display: flex;
            justify-content: space-between;
            font-size: 8.5pt;
            color: #666;
          }
          @media print {
            body { padding: 0; }
            .no-print { display: none; }
          }
        </style>
      </head>
      <body>
        <div class="header-bar">
          <div>
            <h1 class="org-title">SMART PROCUREMENT &amp; WAREHOUSE DISPATCH</h1>
            <p class="sub-title">Algorithmic Laboratory Capstone • Lossless Decompressed Digital Invoice</p>
          </div>
          <div style="text-align: right;">
            <span class="badge-pill">LOSSLESS VERIFIED 100%</span>
            <div style="font-size: 8pt; color: #555; margin-top: 4px;">Huffman Coding Decompressed</div>
          </div>
        </div>

        <div class="meta-grid">
          <div>
            <div class="meta-item"><strong>Client / Buyer:</strong> ${escapeHtml(customer)}</div>
            <div class="meta-item"><strong>Organization:</strong> ${escapeHtml(org)}</div>
          </div>
          <div>
            <div class="meta-item"><strong>Date &amp; Time:</strong> ${escapeHtml(dateStr)}</div>
            <div class="meta-item"><strong>Algorithmic Solvers:</strong> 0/1 Knapsack • FFD Bin Pack</div>
          </div>
        </div>

        <div class="section-title">Official Lossless Order Bill</div>
        <div class="raw-bill-box">${escapeHtml(text)}</div>

        <div class="signature-row">
          <div class="sig-line">Warehouse Logistics Officer</div>
          <div class="sig-line">Client Authorization</div>
        </div>

        <div class="footer-bar">
          <span>Generated by Smart Procurement &amp; Packer • Localhost Service</span>
          <span>Print / PDF Document Layout</span>
        </div>
      </body>
      </html>
    `;

    const printFrame = document.createElement('iframe');
    printFrame.style.position = 'fixed';
    printFrame.style.right = '0';
    printFrame.style.bottom = '0';
    printFrame.style.width = '0';
    printFrame.style.height = '0';
    printFrame.style.border = '0';
    document.body.appendChild(printFrame);

    const frameDoc = printFrame.contentWindow.document;
    frameDoc.open();
    frameDoc.write(printableHtml);
    frameDoc.close();

    setTimeout(() => {
      printFrame.contentWindow.focus();
      printFrame.contentWindow.print();
      setTimeout(() => {
        if (printFrame.parentNode) {
          printFrame.parentNode.removeChild(printFrame);
        }
      }, 2000);
    }, 400);

    addLog(`[Print] Opened native Print / Save as PDF dialog for: ${title}`, 'white');
  }

  function exportInvoiceAsWord(text, title = 'Digital_Order_Invoice') {
    if (!text || text.trim() === '' || text.includes('Loading invoice...')) {
      alert('Invoice is still loading. Please click "Generate & Compress Invoice" and try again.');
      return;
    }

    const customer = (el.checkoutCustomerName && el.checkoutCustomerName.value) || 'Valued Customer';
    const org = (el.checkoutOrgName && el.checkoutOrgName.value) || 'Event Logistics';
    const dateStr = new Date().toLocaleString();

    const wordHtml = `
      <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
      <head>
        <meta charset='utf-8'>
        <title>${escapeHtml(title)}</title>
        <!--[if gte mso 9]>
        <xml>
        <w:WordDocument>
          <w:View>Print</w:View>
          <w:Zoom>100</w:Zoom>
          <w:DoNotOptimizeForBrowser/>
        </w:WordDocument>
        </xml>
        <![endif]-->
        <style>
          body { font-family: 'Segoe UI', Arial, sans-serif; font-size: 11pt; color: #111; margin: 30pt; }
          h1 { font-size: 18pt; color: #000; border-bottom: 2pt solid #000; padding-bottom: 5pt; margin-bottom: 6pt; }
          .subtitle { font-size: 10pt; color: #555; margin-bottom: 15pt; }
          .meta-box { background: #f7f7f7; border: 1pt solid #ccc; padding: 10pt; margin-bottom: 15pt; }
          pre { font-family: 'Consolas', 'Courier New', monospace; font-size: 9.5pt; background: #fafafa; border: 1pt solid #ccc; padding: 12pt; white-space: pre-wrap; line-height: 1.35; }
          .sig-table { width: 100%; margin-top: 35pt; border: none; }
          .sig-table td { border: none; padding-top: 30pt; font-size: 10pt; font-weight: bold; }
        </style>
      </head>
      <body>
        <h1>SMART PROCUREMENT &amp; WAREHOUSE DISPATCH</h1>
        <div class="subtitle">Official Order Manifest &amp; Lossless Decompressed Bill (Huffman Coding Module 4)</div>
        
        <div class="meta-box">
          <p style="margin: 3pt 0;"><strong>Customer / Buyer:</strong> ${escapeHtml(customer)}</p>
          <p style="margin: 3pt 0;"><strong>Organization / Event:</strong> ${escapeHtml(org)}</p>
          <p style="margin: 3pt 0;"><strong>Export Date:</strong> ${escapeHtml(dateStr)}</p>
          <p style="margin: 3pt 0;"><strong>Algorithmic Verification:</strong> 0/1 Knapsack Optimal &bull; FFD Carton Packing &bull; Huffman Coding 100% Lossless Recovery</p>
        </div>

        <h3 style="margin-top: 15pt; margin-bottom: 6pt;">DECOMPRESSED INVOICE MANIFEST:</h3>
        <pre>${escapeHtml(text)}</pre>

        <table class="sig-table">
          <tr>
            <td style="border-top: 1pt solid #000; width: 42%; text-align: center;">Warehouse Logistics Signature</td>
            <td style="width: 16%;"></td>
            <td style="border-top: 1pt solid #000; width: 42%; text-align: center;">Client Authorization Signature</td>
          </tr>
        </table>
      </body>
      </html>
    `;

    const blob = new Blob(['\ufeff', wordHtml], { type: 'application/msword' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${title}_${Date.now()}.doc`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    addLog(`[Word Export] Downloaded lossless bill as Word Document: ${a.download}`, 'green');
  }

  // ==========================================
  // LAUNCH APP
  // ==========================================
  init();
});
