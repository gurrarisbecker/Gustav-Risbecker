#include <stdint.h>
#include <unistd.h>
#include <stdio.h>

////////////// DEFINITIONS SAVED

// Adress fpr switchs and button
#define SWI 0x04000010
#define BTN 0x040000d0
#define DISPLAYSTART 0x04000050

#define VRAM_BASE 0x08000000u
#define WIDTH 320
#define HEIGHT 240

// Configure grid dimensions
#define GRID_COLS 16                // 32 cells horizontally
#define GRID_ROWS 12                // 24 cells vertically
#define CELL_W (WIDTH / GRID_COLS)  // 10 pixels wide
#define CELL_H (HEIGHT / GRID_ROWS) // 10 pixels tall

// Timer registers
#define TIMER_BASE 0x04000020
#define TIMER_STATUS (*(volatile int *)(TIMER_BASE + 0x00))
#define TIMER_CTRL (*(volatile int *)(TIMER_BASE + 0x04))
#define TIMER_PERIODL (*(volatile int *)(TIMER_BASE + 0x08))
#define TIMER_PERIODH (*(volatile int *)(TIMER_BASE + 0x0C))

#define MAX_CELLS 100

extern void enable_interrupt();

int score = 0;
int snakedir_x = 1; // moving right as default
int snakedir_y = 0;
int timeoutcount2 = 0;
int SPEED = 5;

////////////// BOARD LOGICS

static uint8_t board[GRID_COLS * GRID_ROWS];

static inline void set_cell(int x, int y, uint8_t val)
{
    board[y * GRID_COLS + x] = val;
}

static inline uint8_t get_cell(int x, int y)
{
    return board[y * GRID_COLS + x];
}

////////////// SNAKE STRUCTURE

typedef struct Node
{
    int x, y;
    struct Node *next;
} Node;

static Node *snake_head = 0;
static Node *snake_tail = 0;

static Node node_pool[MAX_CELLS];
static int node_pool_used = 0;

// allocate one node from the pool
static inline Node *node_alloc(void)
{
    if (node_pool_used >= MAX_CELLS)
        return 0;
    return &node_pool[node_pool_used++];
}

////////////// MISC UTILITIES

static inline uint8_t rgb332(uint8_t r, uint8_t g, uint8_t b)
{
    return (uint8_t)(((r >> 5) << 5) | ((g >> 5) << 2) | (b >> 6));
}
int random(void)
{
    static uint32_t seed = 53233;
    seed = (8253729 * seed + 2396403 * score);
    return (int)(seed % 32768);
}

static inline uint8_t cell_bg_color(int gx, int gy)
{
    // two shades of green
    const uint8_t LIGHT = rgb332(0, 70, 0);
    const uint8_t DARK = rgb332(0, 110, 0);

    // even/odd
    return (((gx + gy) & 1) == 0) ? LIGHT : DARK;
}

int get_sw1(void)
{
    int stat = (*(volatile uint16_t *)SWI) & 0x1;
    return stat;
}
int get_sw2(void)
{
    int stat = ((*(volatile uint16_t *)SWI) & 0x2) >> 1;
    return stat;
}

int get_btn(void)
{
    return (*(volatile uint16_t *)BTN) & 0x1;
}

void handle_interrupt(unsigned cause){
  if (TIMER_STATUS & 0x1)
  {                       // check if timeout
    TIMER_STATUS = 0x1; // reset flag
    timeoutcount2++;
    if (timeoutcount2 >= SPEED)
    { // 10 ticks = 1 second
      timeoutcount2 = 0;

      int next_x = snake_head->x + snakedir_x;
      int next_y = snake_head->y + snakedir_y;

      check_next_cell(next_x, next_y);
    }
  }

}

////////////// GRAPHIC UTIFILIES

static inline void set_pixel(int x, int y, uint8_t c)
{
    x = x * CELL_W;
    y = y * CELL_H;
    volatile uint8_t *fb = (volatile uint8_t *)VRAM_BASE;
    fb[y * WIDTH + x] = c;
}

static void fill_rect_px(int x, int y, int w, int h, uint8_t c)
{
    volatile uint8_t *fb = (volatile uint8_t *)VRAM_BASE;

    for (int j = 0; j < h; ++j)
    {
        for (int i = 0; i < w; ++i)
        {
            fb[(y + j) * WIDTH + (x + i)] = c;
        }
    }
}

static inline void grid_fill_cell(int gx, int gy, uint8_t c)
{
    int x = gx * CELL_W;
    int y = gy * CELL_H;
    fill_rect_px(x, y, CELL_W, CELL_H, c);
}

////////////// SNAKE LOGICS

// Add a new head at (new_x,new_y). Keep tail (used when eating).
void snake_grow(int new_x, int new_y)
{
    Node *n = node_alloc();
    if (!n)
        return;

    n->x = new_x;
    n->y = new_y;
    n->next = snake_head;
    snake_head = n;
    if (!snake_tail)
        snake_tail = n; // list was empty

    // --- draw new head ---
    set_cell(new_x, new_y, 1);
    grid_fill_cell(new_x, new_y, rgb332(0, 255, 0));
}

void snake_move(int new_x, int new_y)
{
    // we draw the new head at next cell
    set_cell(new_x, new_y, 1);
    grid_fill_cell(new_x, new_y, rgb332(0, 255, 0));
    set_pixel(new_x,new_y, rgb332(255,0,0));

    // traverse through all nodes to find next-to end cell
    Node *old_tail = snake_tail;
    Node *prev = snake_head;
    while (prev->next != old_tail)
        prev = prev->next;

    // we remove this last last cell, the tail, and fill with background
    set_cell(old_tail->x, old_tail->y, 0);
    grid_fill_cell(old_tail->x, old_tail->y, cell_bg_color(old_tail->x, old_tail->y));

    // set the tail to next to last cell, new tail, and set tail o prev, then set prev - next to zero to cut its link
    snake_tail = prev;
    snake_tail->next = 0;

    // reuse the tail, no new nodes needed. fixed previous bug
    old_tail->x = new_x;
    old_tail->y = new_y;
    old_tail->next = snake_head;
    snake_head = old_tail;
}

void snake_change_direction(int *dir_x, int *dir_y)
{
    // Change direction clockwise, from -> to:
    if (*dir_x == 1 && *dir_y == 0)
    { // right -> down
        if (get_sw1() == 1)
        {
            *dir_x = 0;
            *dir_y = -1;
        }
        else
        {
            *dir_x = 0;
            *dir_y = 1;
        }
    }
    else if (*dir_x == 0 && *dir_y == 1)
    { // down -> left
        if (get_sw1() == 1)
        {
            *dir_x = 1;
            *dir_y = 0;
        }
        else
        {
            *dir_x = -1;
            *dir_y = 0;
        }
    }
    else if (*dir_x == -1 && *dir_y == 0)
    { // left -> up
        if (get_sw1() == 1)
        {
            *dir_x = 0;
            *dir_y = 1;
        }
        else
        {
            *dir_x = 0;
            *dir_y = -1;
        }
    }
    else if (*dir_x == 0 && *dir_y == -1)
    { // up -> right
        if (get_sw1() == 1)
        {
            *dir_x = -1;
            *dir_y = 0;
        }
        else
        {
            *dir_x = 1;
            *dir_y = 0;
        }
    }
}

void snake_init()
{

    int start_x = GRID_COLS / 2;
    int start_y = GRID_ROWS / 2;
    snake_head = node_alloc();
    if (snake_head)
    {
        snake_head->x = start_x;
        snake_head->y = start_y;
        snake_head->next = 0;
        snake_tail = snake_head;
        set_cell(start_x, start_y, 1);
        grid_fill_cell(start_x, start_y, rgb332(0, 255, 0)); // draw snake head green
    }
}

////////////// TIMER THINGS

void timerinit(void)
{
    unsigned int period = 3000000; // 3 million for 0.1 sec 30mhz
    TIMER_PERIODL = period & 0xFFFF;
    TIMER_PERIODH = (period >> 16) & 0xFFFF;
    TIMER_CTRL = (1 << 0) | (1 << 1) | (1 << 2);
}

////////////// SCORE THINGS

int get_num(int num)
{
    static const int seven_seg_digits[11] = {
        0xC0, // 0
        0xF9, // 1
        0xA4, // 2
        0xB0, // 3
        0x99, // 4
        0x92, // 5
        0x82, // 6
        0xF8, // 7
        0x80, // 8
        0x90, // 9
        0xFF, // OFF
    };
    return seven_seg_digits[num];
}

void set_disp(int disp_num, int num)
{
    int display_multi = 0x10 * disp_num;
    int adress = display_multi + DISPLAYSTART;
    volatile int *disp = (volatile int *)adress;
    *disp = get_num(num);
}

void show_score(void)
{
    int hundreds = (score / 100) % 10;
    int tens = (score / 10) % 10;
    int ones = score % 10;
    set_disp(3, ones);
    set_disp(4, tens);
    set_disp(5, hundreds);
}

////////////// GAME LOGICS + MISC

void drawScreen(int r, int g, int b)
{
    volatile uint8_t *fb = (volatile uint8_t *)VRAM_BASE;
    uint8_t color = rgb332(r, g, b);

    for (int i = 0; i < WIDTH * HEIGHT; ++i)
        fb[i] = color;
}

void spawn_new_apple()
{
    int apple_x = random() % GRID_COLS;
    int apple_y = random() % GRID_ROWS;
    while (get_cell(apple_x, apple_y) != 0)
    { // try until empty cell is found
        apple_x = random() % GRID_COLS;
        apple_y = random() % GRID_ROWS;
    }
    set_cell(apple_x, apple_y, 2);                       // set apple in grid
    grid_fill_cell(apple_x, apple_y, rgb332(255, 0, 0)); // draw apple
}
void dead(int posx, int posy) {
    int total = GRID_ROWS * GRID_COLS;
    int done = 0;
    int stop = 50000;

    int cx = GRID_COLS /2; // center column
    int cy = GRID_ROWS /2; // center row

    int x = cx;
    int y = cy;

    int dx[4] = {1, 0, -1, 0};
    int dy[4] = {0, 1, 0, -1};

    int dir = 0;       // start moving right
    int step = 1;      // number of steps in current leg
    int step_count = 0;
    int change_dir = 0;

    while (done < total) {
        // Only fill if within bounds
        if (x >= 0 && x < GRID_COLS && y >= 0 && y < GRID_ROWS) {
            grid_fill_cell(x, y, rgb332(255, 0, 0));
            done++;
            stop -= 300;
        }

        // Move to next cell
        x += dx[dir];
        y += dy[dir];
        step_count++;

        // Change direction when current leg finished
        if (step_count == step) {
            step_count = 0;
            dir = (dir + 1) % 4;
            change_dir++;
            if (change_dir % 2 == 0) {
                step++; // increase step every two direction changes
            }
        }

        // small delay so we can see the effect
        for (volatile int i = 0; i < stop; i++);
    }
}

void win_seq(void) {
    int win[12 * 16] = {
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,1,0,1,0,1,1,1,0,1,0,0,1,0,0,
        0,0,1,0,1,0,1,0,1,0,1,0,0,1,0,0,
        0,0,0,1,0,0,1,0,1,0,1,0,0,1,0,0,
        0,0,0,1,0,0,1,1,1,0,1,1,1,1,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,1,0,0,0,1,0,1,0,1,0,0,1,0,1,0,
        0,1,0,0,0,1,0,1,0,1,1,0,1,0,1,0,
        0,1,0,1,0,1,0,1,0,1,0,1,1,0,0,0,
        0,0,1,0,1,0,0,1,0,1,0,0,1,0,1,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    };

    int total = 12 * 16;
    for (int i = 0; i < total; i++) {
        int x = i % 16;
        int y = i / 16;

        if (win[i] == 1) {
            grid_fill_cell(x, y, rgb332(0, 0, 0));  // black
        } else {
            grid_fill_cell(x, y, rgb332(0, 255, 0));  // green
        }

        // small delay to make it flash visible
        for (volatile int j = 0; j < 50000; j++);
    }
    while(1);
}


void check_next_cell(int next_x, int next_y)
{
    if (get_cell(next_x, next_y) == 1 || next_x < 0 || next_x >= GRID_COLS || next_y < 0 || next_y >= GRID_ROWS)
    {
        dead(next_x, next_y); // fill screen red
        print("snake dead"); //debug
        while (1)
            ; // stop game / freeze
    }
    if (get_cell(next_x, next_y) == 2) // it was an apple
    {
        //print("apple eaten"); //debug
        set_cell(next_x, next_y, 0); // remove apple and set snake body
        spawn_new_apple();

        snake_grow(next_x, next_y);
        score++;
        show_score();
        if(score == 100){
          win_seq();
        }
    }
    else
    {
        snake_move(next_x, next_y);
    }
}

////////////// MAIN

int main(void)
{
    for (int i = 0; i <= 5; i++)
    {
        set_disp(i, 10);
    }

    for (int gy = 0; gy < GRID_ROWS; ++gy)
    {
        for (int gx = 0; gx < GRID_COLS; ++gx)
        {

            set_cell(gx, gy, 0);                           // set all cells to void
            grid_fill_cell(gx, gy, cell_bg_color(gx, gy)); // set them all to black just in case.
        }
    }

    int prev_btn = 0;



    snake_init();

    // make the snake 5 blocks long at start (head + 4 tail segments)
    for (int i = 1; i < 5; ++i)
    {
        // place tail segments opposite to the current movement direction
        int tx = snake_head->x - i * snakedir_x;
        int ty = snake_head->y - i * snakedir_y;

        Node *n = node_alloc();

        n->x = tx;
        n->y = ty;
        n->next = 0;

        // link after current tail
        snake_tail->next = n;
        snake_tail = n;

        // draw the new tail cell
        set_cell(tx, ty, 1);
        grid_fill_cell(tx, ty, rgb332(0, 255, 0));
    }

    spawn_new_apple();
    timerinit();
    enable_interrupt();
    while (1)
    { // game loop

        int btn = get_btn();
        if (btn && !prev_btn)
        { // button pressed now but not last time
            snake_change_direction(&snakedir_x, &snakedir_y);
        }
        prev_btn = btn;
        if(get_sw2() == 1){
          SPEED = 2;
        }
        if(get_sw2() == 0){
          SPEED = 5;
        }

    }
}
